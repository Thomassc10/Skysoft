package com.skysoft.features.inventory.itemlist

import com.skysoft.data.skyblock.SkyBlockObtainInfo
import com.skysoft.data.skyblock.SkyBlockRecipe
import com.skysoft.data.skyblock.SkyBlockRecipeType

internal fun SkyBlockObtainInfo.withOnlyMethodsMatching(markers: List<String>): SkyBlockObtainInfo? {
    val methods = summary.split("; ").filter { method ->
        markers.any { marker -> method.contains(marker, ignoreCase = true) }
    }
    return methods.takeIf(List<String>::isNotEmpty)?.let { copy(summary = it.joinToString("; ")) }
}

internal fun SkyBlockObtainInfo.withoutSpecializedMethods(recipes: List<SkyBlockRecipe>): SkyBlockObtainInfo? {
    val hasFusionRecipe = recipes.any { it.type == SkyBlockRecipeType.ATTRIBUTE_FUSION }
    val methods = summary.split("; ").filterNot { method ->
        SPECIALIZED_DETAIL_MARKERS.any { marker -> method.contains(marker, ignoreCase = true) } ||
            hasFusionRecipe && method.startsWith("Fusing ")
    }
    return methods.takeIf(List<String>::isNotEmpty)?.let { copy(summary = it.joinToString("; ")) }
}

internal val SPECIALIZED_DETAIL_MARKERS = listOf("Pocket Black Hole", "Salts")
