package com.skysoft.mixin

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(RecipeBookComponent::class)
interface RecipeBookComponentAccessor {
    @Accessor("visible")
    fun skysoftSetVisible(visible: Boolean)
}
