package com.skysoft.mixin

import com.skysoft.features.helditem.HeldItemTextureOverrides
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable

@Mixin(ItemModelResolver::class)
open class ItemModelResolverMixin {
    @ModifyVariable(method = ["updateForTopItem"], at = At("HEAD"), argsOnly = true, ordinal = 0)
    protected fun skysoftReplaceItemTexture(itemStack: ItemStack): ItemStack =
        HeldItemTextureOverrides.renderStack(itemStack)
}
