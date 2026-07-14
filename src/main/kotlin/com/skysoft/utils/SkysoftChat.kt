package com.skysoft.utils

import com.skysoft.SkysoftMod
import java.net.URI
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextColor

object SkysoftChat {
    private const val PREFIX_LEFT = 0x2BB1FB
    private const val PREFIX_RIGHT = 0x1A87C4
    private const val MESSAGE_BLUE = 0x2BB1FB

    fun chat(message: String): ChatDeliveryResult =
        chat(defaultText(message))

    fun success(message: String): ChatDeliveryResult =
        chat(Component.literal(message).withStyle(ChatFormatting.GREEN))

    fun error(message: String): ChatDeliveryResult =
        chat(Component.literal(message).withStyle(ChatFormatting.RED))

    fun link(message: String, url: String, hover: String = "Open $url"): ChatDeliveryResult =
        chat(
            Component.literal(message).withStyle {
                it.withColor(TextColor.fromRgb(MESSAGE_BLUE))
                    .withUnderlined(true)
                    .withClickEvent(ClickEvent.OpenUrl(URI.create(url)))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(hover).withStyle(ChatFormatting.GRAY)))
            },
        )

    fun chat(message: Component): ChatDeliveryResult {
        val text = prefixed(message)
        val minecraft = Minecraft.getInstance()
        if (minecraft.player == null) {
            SkysoftMod.LOGGER.info(text.string)
            return ChatDeliveryResult.LOGGED_ONLY
        }
        MinecraftClient.chat(minecraft).addClientSystemMessage(text)
        return ChatDeliveryResult.DELIVERED
    }

    fun feedback(source: FabricClientCommandSource, message: String) {
        source.sendFeedback(prefixed(defaultText(message)))
    }

    fun error(source: FabricClientCommandSource, message: String) {
        source.sendError(prefixed(Component.literal(message).withStyle(ChatFormatting.RED)))
    }

    fun prefixed(message: Component): MutableComponent =
        Component.empty().append(prefix()).append(message)

    private fun prefix(): MutableComponent =
        gradient("[Skysoft] ").withStyle(ChatFormatting.BOLD)

    private fun gradient(text: String): MutableComponent {
        val result = Component.empty()
        text.forEachIndexed { index, char ->
            val progress = index.toFloat() / (text.length - 1).coerceAtLeast(1)
            result.append(
                Component.literal(char.toString()).withStyle {
                    it.withColor(TextColor.fromRgb(mix(PREFIX_LEFT, PREFIX_RIGHT, progress)))
                },
            )
        }
        return result
    }

    private fun defaultText(message: String): MutableComponent =
        Component.literal(message).withStyle {
            it.withColor(TextColor.fromRgb(MESSAGE_BLUE))
        }

    private fun mix(start: Int, end: Int, progress: Float): Int {
        val r = channel(start, end, RED_SHIFT, progress)
        val g = channel(start, end, GREEN_SHIFT, progress)
        val b = channel(start, end, BLUE_SHIFT, progress)
        return (r shl RED_SHIFT) or (g shl GREEN_SHIFT) or b
    }

    private fun channel(start: Int, end: Int, shift: Int, progress: Float): Int {
        val from = (start shr shift) and RGB_CHANNEL_MASK
        val to = (end shr shift) and RGB_CHANNEL_MASK
        return (from + (to - from) * progress).toInt().coerceIn(RGB_CHANNEL_MIN, RGB_CHANNEL_MAX)
    }

    private const val RED_SHIFT = 16
    private const val GREEN_SHIFT = 8
    private const val BLUE_SHIFT = 0
    private const val RGB_CHANNEL_MASK = 0xFF
    private const val RGB_CHANNEL_MIN = 0
    private const val RGB_CHANNEL_MAX = 255
}

enum class ChatDeliveryResult {
    DELIVERED,
    LOGGED_ONLY,
}
