package com.skysoft.utils.renderables.decorators

import com.skysoft.utils.render.shader.SkysoftCircleShaderRenderer
import com.skysoft.utils.renderables.GuiRenderable
import com.skysoft.utils.renderables.renderAt
import net.minecraft.client.gui.GuiGraphicsExtractor
import java.awt.Color
import kotlin.math.PI
import kotlin.math.max

class CircularLayoutRenderable(
    private val child: GuiRenderable,
    private val filledColor: Int,
    private val unfilledColor: Int? = null,
    private val filledPercentage: Double = FULL_PERCENT,
    private val padding: Int = 2,
) : GuiRenderable {
    private val radius = max(child.width, child.height) / 2 + padding
    private val takenSpace = (radius - padding) * 2
    override val width: Int = radius * 2
    override val height: Int = radius * 2

    override fun render(context: GuiGraphicsExtractor) {
        if (unfilledColor == null || filledPercentage >= FULL_PERCENT) {
            drawArc(context, filledColor, START_ANGLE_RADIANS, START_ANGLE_RADIANS)
        } else {
            val boundary = (START_ANGLE_RADIANS + missingArcRadians()).mod(TAU)
            drawArc(context, filledColor, START_ANGLE_RADIANS, boundary)
            drawArc(context, unfilledColor, boundary, START_ANGLE_RADIANS)
        }
        child.renderAt(
            context,
            padding + (takenSpace - child.width) / 2,
            padding + (takenSpace - child.height) / 2,
        )
    }

    private fun missingArcRadians(): Float =
        ((FULL_PERCENT - filledPercentage).coerceIn(0.0, FULL_PERCENT) / FULL_PERCENT * TAU).toFloat()

    private fun drawArc(context: GuiGraphicsExtractor, color: Int, from: Float, to: Float) {
        SkysoftCircleShaderRenderer.drawFilledCircle(
            context,
            0,
            0,
            Color(color, true),
            radius,
            angle1 = from,
            angle2 = to,
        )
    }

    private companion object {
        private const val FULL_PERCENT = 100.0
        private val START_ANGLE_RADIANS = (PI.toFloat() * 3f) / 2f
        private val TAU = (2.0 * PI).toFloat()
    }
}
