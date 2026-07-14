package com.skysoft.features.loot

import java.util.Locale

internal object RareLootItemIds {
    fun fromDisplayName(displayName: String): String? =
        displayName
            .replace(Regex("""§."""), "")
            .uppercase(Locale.US)
            .replace(Regex("""[^A-Z0-9]+"""), "_")
            .trim('_')
            .takeIf { it.isNotBlank() }
            ?.let { itemId -> displayNameOverrides[itemId] ?: itemId }

    private val displayNameOverrides = mapOf(
        "CARROT" to "CARROT_ITEM",
        "CARROTS" to "CARROT_ITEM",
        "POTATO" to "POTATO_ITEM",
        "POTATOES" to "POTATO_ITEM",
    )
}
