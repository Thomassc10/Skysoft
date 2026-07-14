package com.skysoft.utils.render

import com.skysoft.utils.WorldVec
import com.skysoft.utils.toWorldVec
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityAttachment
import net.minecraft.world.entity.Entity

object EntityLabelRenderer {
    fun draw(
        context: SkysoftRenderContext,
        entity: Entity,
        lines: List<Component>,
        style: WorldLabelStyle = WorldLabelStyle(),
        yOffset: Double = DEFAULT_Y_OFFSET,
    ) {
        if (lines.isEmpty()) return
        WorldLabelRenderer.draw(context, entity.bodyAnchor(context, yOffset), lines, style)
    }

    fun drawAboveNameTag(
        context: SkysoftRenderContext,
        entity: Entity,
        lines: List<Component>,
        style: WorldLabelStyle = WorldLabelStyle(),
        reservedNameTagLines: Int = DEFAULT_RESERVED_NAME_TAG_LINES,
    ) {
        if (lines.isEmpty()) return
        WorldLabelRenderer.drawAbove(
            context,
            entity.nameTagAnchor(context),
            lines,
            style,
            reservedLinesBelow = reservedNameTagLines,
        )
    }

    private fun Entity.bodyAnchor(context: SkysoftRenderContext, yOffset: Double): WorldVec {
        val position = getPosition(context.partialTicks).toWorldVec()
        return WorldVec(position.x, position.y + bbHeight + yOffset, position.z)
    }

    private fun Entity.nameTagAnchor(context: SkysoftRenderContext): WorldVec {
        val position = getPosition(context.partialTicks).toWorldVec()
        val attachment = attachments.get(EntityAttachment.NAME_TAG, 0, getYRot(context.partialTicks)).toWorldVec()
        return position + attachment
    }

    private const val DEFAULT_Y_OFFSET = 0.7
    private const val DEFAULT_RESERVED_NAME_TAG_LINES = 1
}
