package com.skysoft.utils.renderables.animated

import com.skysoft.utils.gui.OrbitDirection
import com.skysoft.utils.renderables.GuiRenderable
import com.skysoft.utils.renderables.renderAt
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.cos
import kotlin.math.sin

class OrbitLayoutRenderable(
    private val root: GuiRenderable,
    private val subBodies: List<GuiRenderable>,
    private val subBodySpacing: Int,
    private val orbitSpeed: Int,
    private val orbitDirection: OrbitDirection,
    private val startedAtNanos: Long = System.nanoTime(),
) : GuiRenderable {
    private val subWidth = subBodies.maxOfOrNull { it.width } ?: 0
    private val subHeight = subBodies.maxOfOrNull { it.height } ?: 0
    private val orbitRadius = root.width.toFloat() / 2 + subBodySpacing + subWidth.toFloat() / 2

    override val width: Int = root.width + (subWidth + subBodySpacing) * 2
    override val height: Int = root.height + (subHeight + subBodySpacing) * 2

    override fun render(context: GuiGraphicsExtractor) {
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2
        root.renderAt(context, centerX - root.width.toFloat() / 2, centerY - root.height.toFloat() / 2)
        if (subBodies.isEmpty()) return
        val elapsedSeconds = (System.nanoTime() - startedAtNanos) / NANOS_PER_SECOND
        val direction = orbitDirection.multiplier
        val baseAngle = Math.toRadians(elapsedSeconds * orbitSpeed * direction)
        val angleStep = TAU / subBodies.size
        subBodies.forEachIndexed { index, body ->
            val angle = baseAngle + angleStep * index
            val x = centerX + cos(angle).toFloat() * orbitRadius - body.width.toFloat() / 2
            val y = centerY + sin(angle).toFloat() * orbitRadius - body.height.toFloat() / 2
            body.renderAt(context, x, y)
        }
    }

    private companion object {
        private const val NANOS_PER_SECOND = 1_000_000_000.0
        private const val TAU = Math.PI * 2.0
    }
}
