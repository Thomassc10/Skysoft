package com.skysoft.features.loot

internal data class RareLootDrop(
    val itemId: String?,
    val displayName: String,
    val amount: Int = 1,
    val featureSource: String,
    val context: String? = null,
)
