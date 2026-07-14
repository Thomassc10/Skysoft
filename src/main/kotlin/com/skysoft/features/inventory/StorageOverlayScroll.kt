package com.skysoft.features.inventory

internal fun centerPage(measurements: Measurements, layoutResult: PageLayoutResult, pageIndex: Int) {
    scroll = centeredScrollForPage(scroll, measurements, layoutResult, pageIndex)
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
