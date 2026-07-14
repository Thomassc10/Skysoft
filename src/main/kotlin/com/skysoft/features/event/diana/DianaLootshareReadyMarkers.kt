package com.skysoft.features.event.diana

import com.skysoft.utils.render.EntityLabelRenderer
import com.skysoft.utils.render.SkysoftRenderContext
import com.skysoft.utils.render.WorldLabelStyle
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor

internal object DianaLootshareReadyMarkers {
    fun mark(playerName: String, now: Long) {
        readyPlayers[playerName.lowercase()] = ReadyPlayer(playerName, now + MARKER_LIFETIME_MILLIS)
    }

    fun clear() {
        readyPlayers.clear()
    }

    fun readyPlayerNames(now: Long): Set<String> {
        prune(now)
        return readyPlayers.values.mapTo(mutableSetOf()) { readyPlayer -> readyPlayer.playerName }
    }

    fun renderWorld(context: SkysoftRenderContext, localPlayerName: String?, now: Long) {
        val readyNames = readyPlayerNames(now).mapTo(mutableSetOf()) { name -> name.lowercase() }
        if (readyNames.isEmpty()) return
        val level = Minecraft.getInstance().level ?: return
        level.players()
            .filter { player -> player.gameProfile.name.lowercase() in readyNames }
            .filterNot { player -> player.gameProfile.name.equals(localPlayerName, ignoreCase = true) }
            .forEach { player ->
                EntityLabelRenderer.drawAboveNameTag(
                    context,
                    player,
                    listOf(CHECKMARK),
                    CHECKMARK_STYLE,
                )
            }
    }

    private fun prune(now: Long) {
        readyPlayers.values.removeIf { readyPlayer -> now >= readyPlayer.expiresAtMillis }
    }

    private data class ReadyPlayer(
        val playerName: String,
        val expiresAtMillis: Long,
    )

    private val readyPlayers = mutableMapOf<String, ReadyPlayer>()
    private val CHECKMARK = Component.literal("✓").withStyle { style ->
        style.withColor(TextColor.fromRgb(LOOTSHARE_READY_COLOR)).withBold(true)
    }
    private val CHECKMARK_STYLE = WorldLabelStyle(maxRenderDistance = 80.0, maxScale = 6.0)
    private const val LOOTSHARE_READY_COLOR = 0x55FFFF
    private const val MARKER_LIFETIME_MILLIS = 75_000L
}
