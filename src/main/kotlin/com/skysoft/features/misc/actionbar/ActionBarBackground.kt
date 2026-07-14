package com.skysoft.features.misc.actionbar

import com.skysoft.SkysoftMod
import com.skysoft.config.SkysoftConfigGui
import com.skysoft.utils.ColorUtilities.ARGB_ALPHA_SHIFT
import com.skysoft.utils.ColorUtilities.COLOR_CHANNEL_MAX
import com.skysoft.utils.MinecraftClient
import com.skysoft.utils.OverlayMessages
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.min

object ActionBarBackground {
    private const val BACKGROUND_RGB = 0x101010
    private const val MAX_ALPHA = 160
    private const val X_PADDING = 4
    private const val Y_PADDING = 3
    private const val FADE_TICKS = 20.0f
    private const val TEXT_Y_FROM_BOTTOM = 72
    private const val FONT_HEIGHT = 9

    fun register() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.OVERLAY_MESSAGE,
            SkysoftMod.id("action_bar_background"),
            ActionBarBackground::render,
        )
    }

    private fun render(context: GuiGraphicsExtractor, tick: DeltaTracker) {
        val minecraft = Minecraft.getInstance()
        if (!SkysoftConfigGui.config().gui.actionBar.background || MinecraftClient.isGuiHidden(minecraft)) {
            return
        }

        val message = OverlayMessages.message(minecraft)
        val time = OverlayMessages.time(minecraft)
        if (message == null || time <= 0) {
            return
        }

        val textWidth = minecraft.font.width(message)
        if (message.string.isBlank() || textWidth <= 0) {
            return
        }

        val alpha = ((time - tick.getGameTimeDeltaPartialTick(false)) * COLOR_CHANNEL_MAX / FADE_TICKS).toInt()
        if (alpha <= 0) {
            return
        }

        val x = (context.guiWidth() - textWidth) / 2
        val textY = context.guiHeight() - TEXT_Y_FROM_BOTTOM
        val color = (min(MAX_ALPHA, alpha) shl ARGB_ALPHA_SHIFT) or BACKGROUND_RGB
        context.nextStratum()
        context.fill(x - X_PADDING, textY - Y_PADDING, x + textWidth + X_PADDING, textY + FONT_HEIGHT + Y_PADDING, color)
    }
}
