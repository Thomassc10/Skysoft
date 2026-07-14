package com.skysoft.features.event.diana

import com.skysoft.config.DianaRareMobOption
import com.skysoft.features.loot.RareLootShareReceipt

internal object DianaLootShareReceipt {
    fun parseMob(message: String): DianaRareMobOption? {
        val receipt = RareLootShareReceipt.target(message) ?: return null
        return DianaRareMobOption.entries.firstOrNull { option ->
            option.matchLabels.any { label -> receipt.contains(label, ignoreCase = true) }
        }
    }
}
