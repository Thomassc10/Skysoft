package com.skysoft.utils

enum class ChangeResult {
    CHANGED,
    UNCHANGED,
    ;

    companion object {
        fun from(changed: Boolean): ChangeResult =
            if (changed) CHANGED else UNCHANGED
    }
}
