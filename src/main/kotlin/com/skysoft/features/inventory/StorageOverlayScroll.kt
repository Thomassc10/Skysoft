package com.skysoft.features.inventory

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

internal fun centerPage(measurements: Measurements, layoutResult: PageLayoutResult, pageIndex: Int) {
    scrollTarget = centeredScrollForPage(scroll, measurements, layoutResult, pageIndex).toDouble()
        .coerceIn(0.0, maxScroll(measurements, layoutResult.contentHeight).toDouble())
}

internal fun centeredScrollForPage(
    currentScroll: Int,
    measurements: Measurements,
    layoutResult: PageLayoutResult,
    pageIndex: Int,
): Int {
    val layout = layoutResult.pages[pageIndex] ?: return currentScroll
    val pageTop = layout.y + currentScroll - measurements.scrollPanel.y
    val centerOffset = (measurements.scrollPanel.height - layout.height).coerceAtLeast(0) / 2
    return (pageTop - centerOffset).coerceIn(0, maxScroll(measurements, layoutResult.contentHeight))
}

internal fun moveStorageScrollTarget(delta: Double, maximum: Int) {
    advanceStorageScroll()
    scrollTarget = (scrollTarget + delta).coerceIn(0.0, maximum.coerceAtLeast(0).toDouble())
}

internal fun advanceStorageScroll(nowNanos: Long = System.nanoTime()) {
    if (lastScrollUpdateNanos == 0L) {
        lastScrollUpdateNanos = nowNanos
        return
    }
    val elapsedMillis = ((nowNanos - lastScrollUpdateNanos).coerceAtLeast(0L) / NANOS_PER_MILLISECOND)
        .coerceAtMost(StorageRuntime.MAX_SCROLL_FRAME_MILLIS)
    lastScrollUpdateNanos = nowNanos
    scrollPosition = smoothedScrollPosition(scrollPosition, scrollTarget, elapsedMillis)
    if (abs(scrollTarget - scrollPosition) <= StorageRuntime.SCROLL_SETTLE_DISTANCE) {
        scrollPosition = scrollTarget
    }
    scroll = scrollPosition.roundToInt()
}

internal fun coerceStorageScroll(maximum: Int) {
    val upperBound = maximum.coerceAtLeast(0).toDouble()
    scrollPosition = scrollPosition.coerceIn(0.0, upperBound)
    scrollTarget = scrollTarget.coerceIn(0.0, upperBound)
    scroll = scrollPosition.roundToInt()
}

internal fun resetStorageScroll() {
    scroll = 0
    scrollPosition = 0.0
    scrollTarget = 0.0
    lastScrollUpdateNanos = 0L
}

internal fun pauseStorageScrollAnimation() {
    lastScrollUpdateNanos = 0L
}

internal fun smoothedScrollPosition(current: Double, target: Double, elapsedMillis: Double): Double {
    if (current == target || elapsedMillis <= 0.0) return current
    val progress = 1.0 - exp(-elapsedMillis / StorageRuntime.SCROLL_RESPONSE_MILLIS)
    return current + (target - current) * progress
}

private const val NANOS_PER_MILLISECOND = 1_000_000.0
