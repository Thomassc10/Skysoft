package com.skysoft.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@JvmInline
value class ElapsedTimeMark(private val millis: Long) {
    fun passedSince(): Duration = (System.currentTimeMillis() - millis).milliseconds

    companion object {
        fun now(): ElapsedTimeMark = ElapsedTimeMark(System.currentTimeMillis())
        fun farPast(): ElapsedTimeMark = ElapsedTimeMark(0L)
    }
}
