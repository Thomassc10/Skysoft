package com.skysoft.utils

import java.awt.Color

enum class LegacyTextColor(private val color: Color) {
    GREEN(Color(85, 255, 85)),
    ;

    fun toColor(): Color = color
}
