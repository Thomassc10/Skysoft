package com.skysoft.features.helditem

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object HeldItemCustomization {
    @JvmStatic
    fun isEligible(itemStack: ItemStack): Boolean = isEligible(itemStack.item)

    internal fun isEligible(item: Item): Boolean = item != Items.MAP && item != Items.FILLED_MAP
}
