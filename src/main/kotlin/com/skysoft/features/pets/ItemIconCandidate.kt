package com.skysoft.features.pets

import net.minecraft.world.item.ItemStack

data class ItemIconCandidate(
    val internalName: String,
    val displayName: String,
    val stack: ItemStack,
)
