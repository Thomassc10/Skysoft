package com.skysoft.features.pets

enum class SkyBlockSkill(val displayName: String, val maxLevel: Int) {
    COMBAT("Combat", 60),
    FARMING("Farming", 60),
    FISHING("Fishing", 50),
    MINING("Mining", 60),
    FORAGING("Foraging", 54),
    ENCHANTING("Enchanting", 60),
    ALCHEMY("Alchemy", 50),
    CARPENTRY("Carpentry", 50),
    TAMING("Taming", 60),
    HUNTING("Hunting", 25),
    ;

    val uppercaseName: String = displayName.uppercase()

    companion object {
        fun getByNameOrNull(name: String): SkyBlockSkill? =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
    }
}
