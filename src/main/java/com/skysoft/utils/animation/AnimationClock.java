package com.skysoft.utils.animation;

import java.util.function.LongSupplier;

public final class AnimationClock {
    private final LongSupplier nanoTime;
    private long startedAtNanos = Long.MIN_VALUE;

    public AnimationClock() {
        this(System::nanoTime);
    }

    AnimationClock(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    public void restart() {
        startedAtNanos = nanoTime.getAsLong();
    }

    public void stop() {
        startedAtNanos = Long.MIN_VALUE;
    }

    public boolean hasStarted() {
        return startedAtNanos != Long.MIN_VALUE;
    }

    public float progress(int durationMillis) {
        if (!hasStarted()) {
            return 1.0f;
        }
        if (durationMillis <= 0) {
            return 1.0f;
        }

        long durationNanos = durationMillis * 1_000_000L;
        long elapsedNanos = Math.max(0L, nanoTime.getAsLong() - startedAtNanos);
        return Math.min(elapsedNanos / (float) durationNanos, 1.0f);
    }
}
