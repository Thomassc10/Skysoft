package com.skysoft.mixin

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Mutable
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(AbstractContainerScreen::class)
interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    fun skysoftGetLeftPos(): Int

    @Accessor("topPos")
    fun skysoftGetTopPos(): Int

    @Accessor("topPos")
    fun skysoftSetTopPos(topPos: Int)

    @Accessor("imageWidth")
    fun skysoftGetImageWidth(): Int

    @Accessor("imageHeight")
    fun skysoftGetImageHeight(): Int

    @Mutable
    @Accessor("imageHeight")
    fun skysoftSetImageHeight(imageHeight: Int)

    @Accessor("inventoryLabelY")
    fun skysoftSetInventoryLabelY(inventoryLabelY: Int)

    @Accessor("skipNextRelease")
    fun skysoftSetSkipNextRelease(skipNextRelease: Boolean)

    @Accessor("hoveredSlot")
    fun skysoftGetHoveredSlot(): Slot?

    @Invoker("extractSlot")
    fun skysoftExtractSlot(context: GuiGraphicsExtractor, slot: Slot, mouseX: Int, mouseY: Int)

    @Invoker("slotClicked")
    fun skysoftSlotClicked(slot: Slot?, slotId: Int, button: Int, action: ContainerInput)
}
