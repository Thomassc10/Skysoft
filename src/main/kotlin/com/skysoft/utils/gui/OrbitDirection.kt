package com.skysoft.utils.gui

enum class OrbitDirection(private val displayName: String, val multiplier: Int) {
    CLOCKWISE("Clockwise", 1),
    COUNTER_CLOCKWISE("Counter-clockwise", -1),
    ;

    override fun toString(): String = displayName
}
