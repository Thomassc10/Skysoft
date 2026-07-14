package com.skysoft.utils

class SmoothFloatTransition(
    initialValue: Float,
    private val durationNanos: Long,
) {
    private var startValue = initialValue
    private var targetValue = initialValue
    private var startedAt = 0L

    fun value(target: Float, nowNanos: Long = System.nanoTime()): Float {
        val current = currentValue(nowNanos)
        if (target != targetValue) {
            startValue = current
            targetValue = target
            startedAt = nowNanos
        }
        return currentValue(nowNanos)
    }

    private fun currentValue(nowNanos: Long): Float {
        if (startValue == targetValue || startedAt == 0L) return targetValue
        val progress = ((nowNanos - startedAt).toDouble() / durationNanos).coerceIn(0.0, 1.0)
        val eased = EasingUtilities.smoothStep(progress).toFloat()
        return startValue + (targetValue - startValue) * eased
    }
}
