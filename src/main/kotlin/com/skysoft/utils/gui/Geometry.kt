package com.skysoft.utils.gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.item.ItemStack

data class Rect(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun contains(mouseX: Int, mouseY: Int): Boolean = mouseX in x until x + width && mouseY in y until y + height
    fun intersects(other: Rect): Boolean =
        x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y
}

data class Point(val x: Int, val y: Int)

internal fun GuiGraphicsExtractor.itemWithDecorations(stack: ItemStack, x: Int, y: Int) {
    if (stack.isEmpty) return
    item(stack, x, y)
    itemDecorations(net.minecraft.client.Minecraft.getInstance().font, stack, x, y)
}
