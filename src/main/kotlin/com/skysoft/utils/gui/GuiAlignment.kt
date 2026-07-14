package com.skysoft.utils.gui

object GuiAlignment {
    enum class HorizontalAlignment(private val displayName: String) {
        LEFT("Left"),
        CENTER("Center"),
        RIGHT("Right"),
        DONT_ALIGN("Don't Align"),
        ;

        override fun toString(): String = displayName
    }

    enum class VerticalAlignment(private val displayName: String) {
        TOP("Top"),
        CENTER("Center"),
        BOTTOM("Bottom"),
        DONT_ALIGN("Don't Align"),
        ;

        override fun toString(): String = displayName
    }
}
