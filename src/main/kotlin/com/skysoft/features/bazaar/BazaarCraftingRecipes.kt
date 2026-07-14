package com.skysoft.features.bazaar

import com.skysoft.data.skyblock.SkyBlockDataLoadState
import com.skysoft.data.skyblock.SkyBlockDataRepository
import com.skysoft.data.skyblock.SkyBlockRecipe

internal data class BazaarCraftingRecipe(
    val outputId: String,
    val outputCount: Long,
    val ingredients: Map<String, Long>,
)

internal object BazaarCraftingRecipes {
    fun load() = SkyBlockDataRepository.register()

    fun recipesFor(outputId: String): List<BazaarCraftingRecipe>? {
        val status = SkyBlockDataRepository.status.state
        if (status != SkyBlockDataLoadState.READY) return null
        return SkyBlockDataRepository.recipesFor(SkyBlockDataRepository.itemKey(outputId))
            .filterIsInstance<SkyBlockRecipe.Crafting>()
            .map { recipe ->
                BazaarCraftingRecipe(
                    outputId = recipe.result.id,
                    outputCount = recipe.result.count,
                    ingredients = recipe.ingredients.groupingBy { it.id }.fold(0L) { total, ingredient ->
                        total + ingredient.count
                    },
                )
            }
    }
}
