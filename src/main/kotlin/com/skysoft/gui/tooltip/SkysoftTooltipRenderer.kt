package com.skysoft.gui.tooltip

import com.skysoft.config.SkysoftConfigGui
import com.skysoft.gui.scale.GuiScaleController
import com.skysoft.utils.MinecraftClient
import com.skysoft.utils.render.LegacyTextRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.roundToInt

object SkysoftTooltipRenderer {
    private const val BACKGROUND = 0xE0101010.toInt()
    private const val SOLID_BACKGROUND = 0xFF101010.toInt()
    private const val BORDER = 0x80505050.toInt()
    private const val TEXT_COLOR = 0xFFE0E0E0.toInt()
    private const val SCREEN_MARGIN = 4
    private const val PADDING_X = 4
    private const val PADDING_TOP = 4
    private const val LINE_HEIGHT = 10
    private const val EXTRA_HEIGHT = 6
    private const val MOUSE_OFFSET = 12

    fun render(context: GuiGraphicsExtractor, lines: List<String>, x: Int, y: Int) {
        if (lines.isEmpty()) return
        val window = Minecraft.getInstance().window
        val width = lines.maxOfOrNull { LegacyTextRenderer.width(it) } ?: 0
        val height = lines.size * LINE_HEIGHT + EXTRA_HEIGHT
        val clampedX = x.coerceIn(SCREEN_MARGIN, maxOf(SCREEN_MARGIN, window.guiScaledWidth - width - SCREEN_MARGIN))
        val clampedY = y.coerceIn(SCREEN_MARGIN, maxOf(SCREEN_MARGIN, window.guiScaledHeight - height - SCREEN_MARGIN))
        val left = clampedX - PADDING_X
        val top = clampedY - PADDING_TOP
        val right = clampedX + width + PADDING_X
        val bottom = clampedY + height
        context.fill(
            left,
            top,
            right,
            bottom,
            if (SkysoftConfigGui.config().misc.solidTooltipBackground) SOLID_BACKGROUND else BACKGROUND,
        )
        context.outline(left, top, right - left, bottom - top, BORDER)
        lines.forEachIndexed { index, line ->
            LegacyTextRenderer.draw(context, line, clampedX, clampedY + index * LINE_HEIGHT, shadow = false, defaultColor = TEXT_COLOR)
        }
    }

    fun renderAtNormalGuiScale(context: GuiGraphicsExtractor, lines: List<String>, mouseX: Int, mouseY: Int) {
        if (lines.isEmpty()) return
        val minecraft = Minecraft.getInstance()
        val window = minecraft.window
        val activeGuiScale = window.guiScale.coerceAtLeast(1)
        val normalGuiScale = GuiScaleController.resolve(MinecraftClient.screen(minecraft), window).normal()
        val normalMouseX = (mouseX * activeGuiScale / normalGuiScale.toFloat()).roundToInt()
        val normalMouseY = (mouseY * activeGuiScale / normalGuiScale.toFloat()).roundToInt()
        val poseScale = normalGuiScale / activeGuiScale.toFloat()

        context.pose().pushMatrix()
        val previousGuiScale = window.guiScale
        try {
            context.pose().scale(poseScale, poseScale)
            if (previousGuiScale != normalGuiScale) window.setGuiScale(normalGuiScale)
            render(context, lines, normalMouseX + MOUSE_OFFSET, normalMouseY + MOUSE_OFFSET)
        } finally {
            if (window.guiScale != previousGuiScale) window.setGuiScale(previousGuiScale)
            context.pose().popMatrix()
        }
    }
}
