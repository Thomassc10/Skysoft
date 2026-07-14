package com.skysoft.features.pets

import com.skysoft.data.skyblock.SkyBlockRarity

internal object PetLevels {
    fun fullTree(petInternalName: String): List<Int> {
        val constants = PetRepoCache.petsJson ?: return emptyList()
        val properName = PetInternalNames.properName(petInternalName)
        return constants.basePetLeveling + constants.customPetLeveling[properName]?.petLevels.orEmpty()
    }

    fun rarityOffset(petInternalName: String): Int? {
        val (properName, rarity) = PetInternalNames.split(petInternalName) ?: return null
        PetRepoCache.petsJson?.customPetLeveling?.get(properName)?.rarityOffset?.get(rarity)?.let { return it }
        return DEFAULT_RARITY_OFFSETS[rarity]
    }

    private val DEFAULT_RARITY_OFFSETS = mapOf(
        SkyBlockRarity.COMMON to 0,
        SkyBlockRarity.UNCOMMON to 6,
        SkyBlockRarity.RARE to 11,
        SkyBlockRarity.EPIC to 16,
        SkyBlockRarity.LEGENDARY to 20,
        SkyBlockRarity.MYTHIC to 20,
    )
}
