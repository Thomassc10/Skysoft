package com.skysoft.features.inventory.itemlist

import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

internal fun ItemStack.withActionHint(
    action: String,
    displayName: String? = null,
    displayColor: ChatFormatting? = null,
): ItemStack = copy().apply {
    if (displayName != null) {
        set(
            DataComponents.CUSTOM_NAME,
            Component.literal(displayName).withStyle { style ->
                style.withItalic(false).let { if (displayColor == null) it else it.withColor(displayColor) }
            },
        )
    }
    val lore = get(DataComponents.LORE)?.lines().orEmpty() + listOf(
        Component.empty(),
        Component.literal(action).withStyle { style ->
            style.withColor(ChatFormatting.YELLOW).withBold(true).withItalic(false)
        },
    )
    set(DataComponents.LORE, ItemLore(lore))
}
