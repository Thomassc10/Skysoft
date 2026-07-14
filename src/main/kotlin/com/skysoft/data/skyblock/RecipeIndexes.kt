package com.skysoft.data.skyblock

internal fun buildUsageIndex(
    recipes: List<SkyBlockRecipe>,
    keyFor: (RecipeIngredient) -> ItemListEntryKey?,
): Map<ItemListEntryKey, List<SkyBlockRecipe>> = recipes.flatMap { recipe ->
    recipe.ingredients.mapNotNull(keyFor).distinct().map { it to recipe }
}.groupBy({ it.first }, { it.second })
