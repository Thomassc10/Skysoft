package com.skysoft.config

import java.util.Locale

enum class WaypointLabelFormat(private val displayName: String) {
    CAPS("CAPS"),
    LOWERCASE("nocaps"),
    REGULAR("Regular"),
    ;

    fun format(label: String): String = when (this) {
        CAPS -> label.uppercase(Locale.ROOT)
        LOWERCASE -> label.lowercase(Locale.ROOT)
        REGULAR -> label
    }

    override fun toString(): String = displayName
}
