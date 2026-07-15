package com.skysoft.features.inventory.itemlist

import com.skysoft.data.skyblock.RecipeIngredient
import com.skysoft.data.skyblock.expandedOptions

internal fun displayedRecipeIngredient(
    ingredient: RecipeIngredient,
    nowMillis: Long = System.currentTimeMillis(),
): RecipeIngredient {
    val options = ingredient.expandedOptions()
    val index = ((nowMillis / ALTERNATIVE_INGREDIENT_INTERVAL_MILLIS) % options.size).toInt()
    return options[index]
}

private const val ALTERNATIVE_INGREDIENT_INTERVAL_MILLIS = 1_000L
