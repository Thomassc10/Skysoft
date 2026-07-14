package com.skysoft.features.fishing

import com.skysoft.config.FishingHotspotLabelFormat
import com.skysoft.utils.WorldVec
import com.skysoft.utils.chat.ChatMessageSender
import com.skysoft.utils.toWorldVec
import com.skysoft.utils.render.BlockHighlightRenderer
import com.skysoft.utils.render.SkysoftRenderContext
import com.skysoft.utils.render.WorldLabelRenderer
import com.skysoft.utils.render.WorldLabelStyle
import com.skysoft.utils.render.WorldLineRenderer
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import java.awt.Color

internal data class FishingHotspotWaypoint(
    val share: FishingHotspotShare,
    val sharedBy: ChatMessageSender,
    val expiresAtMillis: Long,
)

internal fun shouldRemoveFishingHotspotWaypoint(
    waypoint: FishingHotspotWaypoint,
    playerLocation: WorldVec?,
    now: Long,
): Boolean {
    if (waypoint.expiresAtMillis <= now) return true
    val position = playerLocation ?: return false
    val blockCenter = waypoint.share.location + WAYPOINT_BLOCK_CENTER
    val dx = position.x - blockCenter.x
    val dz = position.z - blockCenter.z
    return dx * dx + dz * dz <= ARRIVAL_HORIZONTAL_RANGE_SQ &&
        kotlin.math.abs(position.y - waypoint.share.location.y) <= ARRIVAL_VERTICAL_RANGE
}

internal fun fishingHotspotLabel(
    labelFormat: FishingHotspotLabelFormat,
    isBold: Boolean,
): Component = if (isBold) {
    Component.literal(labelFormat.format("Hotspot"))
        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
} else {
    Component.literal(labelFormat.format("Hotspot")).withStyle(ChatFormatting.LIGHT_PURPLE)
}

internal object FishingHotspotWaypointRenderer {
    fun renderWorld(
        context: SkysoftRenderContext,
        waypoints: Collection<FishingHotspotWaypoint>,
        drawCrosshairLine: Boolean,
        boldLabel: Boolean,
        labelFormat: FishingHotspotLabelFormat,
    ) {
        waypoints
            .sortedByDescending { distanceSq(context, it.share.location) }
            .forEach { waypoint ->
                val blockCenter = waypoint.share.location + WAYPOINT_BLOCK_CENTER
                BlockHighlightRenderer.drawBlock(
                    context,
                    waypoint.share.location,
                    WAYPOINT_COLOR,
                    FILL_COLOR,
                    WAYPOINT_LINE_WIDTH,
                )
                drawWaypointText(context, waypoint, boldLabel, labelFormat)
                if (drawCrosshairLine) {
                    WorldLineRenderer.drawToCrosshair(context, blockCenter, WAYPOINT_COLOR)
                }
            }
    }

    private fun drawWaypointText(
        context: SkysoftRenderContext,
        waypoint: FishingHotspotWaypoint,
        boldLabel: Boolean,
        labelFormat: FishingHotspotLabelFormat,
    ) {
        WorldLabelRenderer.draw(
            context,
            waypoint.share.location + TEXT_ANCHOR_OFFSET,
            waypoint.labelLines(boldLabel, labelFormat),
            HOTSPOT_LABEL_STYLE,
        )
    }

    private fun FishingHotspotWaypoint.labelLines(
        boldLabel: Boolean,
        labelFormat: FishingHotspotLabelFormat,
    ): List<Component> =
        listOf(
            fishingHotspotLabel(labelFormat, boldLabel),
            Component.literal(share.cleanStat).withStyle(ChatFormatting.AQUA),
            Component.literal("Shared by ")
                .withStyle(ChatFormatting.GRAY)
                .append(sharedBy.nameComponent()),
        )

    private fun distanceSq(context: SkysoftRenderContext, location: WorldVec): Double {
        val delta = location - context.camera.position().toWorldVec()
        return delta.x * delta.x + delta.y * delta.y + delta.z * delta.z
    }

    private const val WAYPOINT_LINE_WIDTH = 3
    private val WAYPOINT_COLOR = Color(255, 85, 255, 220)
    private val FILL_COLOR = Color(170, 0, 255, 70)
    private val HOTSPOT_LABEL_STYLE = WorldLabelStyle()
    private val TEXT_ANCHOR_OFFSET = WorldVec(0.5, 1.8, 0.5)
}

internal const val FISHING_HOTSPOT_WAYPOINT_LIFETIME_MILLIS = 20_000L
private const val ARRIVAL_HORIZONTAL_RANGE_SQ = 20.0 * 20.0
private const val ARRIVAL_VERTICAL_RANGE = 12.0
private val WAYPOINT_BLOCK_CENTER = WorldVec(0.5, 0.5, 0.5)
