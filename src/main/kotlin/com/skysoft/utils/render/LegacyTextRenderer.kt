package com.skysoft.utils.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.StringDecomposer

object LegacyTextRenderer {
    fun width(text: String): Int = Minecraft.getInstance().font.width(text)

    fun draw(
        context: GuiGraphicsExtractor,
        text: String,
        x: Int,
        y: Int,
        shadow: Boolean = true,
        defaultColor: Int = 0xFFFFFFFF.toInt(),
    ) {
        context.text(Minecraft.getInstance().font, text, x, y, defaultColor, shadow)
    }

    fun stripFormatting(text: String): String = text.replace(Regex("§."), "")

    fun formattedSequence(text: String): FormattedCharSequence =
        FormattedCharSequence { sink -> StringDecomposer.iterateFormatted(text, Style.EMPTY, sink) }
}
