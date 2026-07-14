package com.skysoft.features.helditem

import com.skysoft.SkysoftMod
import com.skysoft.config.SkysoftConfigGui
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object HeldItemTextureOverrides {
    private val missingVanillaModelItemIds = mutableSetOf<String>()

    @JvmStatic
    fun renderStack(itemStack: ItemStack): ItemStack {
        val config = SkysoftConfigGui.config().gui.heldItem
        if (!config.enabled || (!config.usesVanillaTexture(null) && config.itemTextureModes.isEmpty())) return itemStack
        return vanillaStackIfConfigured(itemStack)
    }

    fun previewStack(itemStack: ItemStack): ItemStack = vanillaStackIfConfigured(itemStack)

    fun hasPackTexture(itemStack: ItemStack): Boolean {
        val model = itemStack.get(DataComponents.ITEM_MODEL) ?: return false
        val vanillaModel = itemStack.prototype.get(DataComponents.ITEM_MODEL)
        return isHypixelPackModel(model, vanillaModel)
    }

    fun usesVanillaTexture(itemStack: ItemStack): Boolean {
        if (!canUseVanillaTexture(itemStack)) return false
        HeldItemEditorScreen.previewUsesVanillaTexture(itemStack)?.let { return it }
        return SkysoftConfigGui.config().gui.heldItem.usesVanillaTexture(HeldItemTransforms.itemId(itemStack))
    }

    fun canUseVanillaTexture(itemStack: ItemStack): Boolean = hasPackTexture(itemStack) && !isPaper(itemStack)

    fun isPaper(itemStack: ItemStack): Boolean = isPaperItem(itemStack.item)

    internal fun isPaperItem(item: Item): Boolean = item == Items.PAPER

    internal fun isHypixelPackModel(model: Identifier, vanillaModel: Identifier?): Boolean =
        model != vanillaModel && model.namespace == HYPIXEL_MODEL_NAMESPACE

    private fun vanillaStackIfConfigured(itemStack: ItemStack): ItemStack {
        if (!usesVanillaTexture(itemStack)) return itemStack
        val vanillaModel = itemStack.prototype.get(DataComponents.ITEM_MODEL)
        if (vanillaModel == null) {
            val itemId = HeldItemTransforms.itemId(itemStack) ?: itemStack.item.toString()
            if (missingVanillaModelItemIds.add(itemId)) {
                SkysoftMod.LOGGER.warn("Cannot restore the vanilla item model for {}", itemId)
            }
            return itemStack
        }
        return itemStack.copy().also { it.set(DataComponents.ITEM_MODEL, vanillaModel) }
    }

    private const val HYPIXEL_MODEL_NAMESPACE = "hypixel_skyblock"
}
