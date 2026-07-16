package com.skysoft.utils.animation

import java.util.function.LongSupplier

class AnimationClock internal constructor(
    private val nanoTime: LongSupplier,
) {
    constructor() : this(LongSupplier(System::nanoTime))

    private var startedAtNanos = Long.MIN_VALUE

    fun restart() {
        startedAtNanos = nanoTime.getAsLong()
    }

    fun stop() {
        startedAtNanos = Long.MIN_VALUE
    }

    fun hasStarted(): Boolean = startedAtNanos != Long.MIN_VALUE

    fun progress(durationMillis: Int): Float {
        if (!hasStarted() || durationMillis <= 0) return 1.0f
        val durationNanos = durationMillis.toLong() * NANOS_PER_MILLISECOND
        val elapsedNanos = (nanoTime.getAsLong() - startedAtNanos).coerceAtLeast(0L)
        return (elapsedNanos / durationNanos.toFloat()).coerceAtMost(1.0f)
    }

    private companion object {
        const val NANOS_PER_MILLISECOND = 1_000_000L
    }
}
