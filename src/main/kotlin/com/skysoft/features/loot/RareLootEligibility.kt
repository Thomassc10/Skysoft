package com.skysoft.features.loot

internal object RareLootEligibility {
    fun shouldShare(threshold: RareLootThreshold, value: RareLootValue?): Boolean =
        threshold.coins == 0.0 || value?.coins?.let { it >= threshold.coins } == true
}
