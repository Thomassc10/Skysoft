package com.skysoft.gui.tooltip

import com.skysoft.utils.render.LegacyTextRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner

object SkysoftNativeTooltip {
    fun setForNextFrame(context: GuiGraphicsExtractor, lines: List<String>, mouseX: Int, mouseY: Int) {
        if (lines.isEmpty()) return
        context.setTooltipForNextFrame(
            Minecraft.getInstance().font,
            lines.map(LegacyTextRenderer::formattedSequence),
            DefaultTooltipPositioner.INSTANCE,
            mouseX,
            mouseY,
            true,
        )
    }
}
