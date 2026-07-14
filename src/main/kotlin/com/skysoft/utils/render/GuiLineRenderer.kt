package com.skysoft.utils.render

import net.minecraft.client.gui.GuiGraphicsExtractor

object GuiLineRenderer {
    fun drawStep(context: GuiGraphicsExtractor, startX: Int, startY: Int, endX: Int, endY: Int, color: Int) {
        context.fill(minOf(startX, endX), startY, maxOf(startX, endX) + 1, startY + 1, color)
        if (startY != endY) {
            context.fill(endX, minOf(startY, endY), endX + 1, maxOf(startY, endY) + 1, color)
        }
    }
}
