package com.skysoft.features.misc.blockoverlay

object BlockOverlayRules {
    @Volatile
    var version: Long = 0
        private set

    fun markChanged() {
        version++
    }
}
