package com.skysoft.data.skyblock

internal fun buildUsageIndex(
    recipes: List<SkyBlockRecipe>,
    keyFor: (RecipeIngredient) -> ItemListEntryKey?,
): Map<ItemListEntryKey, List<SkyBlockRecipe>> = recipes.flatMap { recipe ->
    recipe.ingredients.flatMap { ingredient ->
        ingredient.expandedOptions().mapNotNull(keyFor)
    }.distinct().map { it to recipe }
}.groupBy({ it.first }, { it.second })

internal fun RecipeIngredient.expandedOptions(): List<RecipeIngredient> =
    listOf(copy(alternatives = emptyList())) + alternatives.map { option ->
        copy(id = option.id, count = option.count, alternatives = emptyList())
    }
