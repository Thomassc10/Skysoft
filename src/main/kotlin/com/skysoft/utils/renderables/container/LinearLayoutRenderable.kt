package com.skysoft.utils.renderables.container

import com.skysoft.utils.gui.GuiAlignment
import com.skysoft.utils.renderables.GuiRenderable
import com.skysoft.utils.renderables.horizontalOffset
import com.skysoft.utils.renderables.renderAt
import com.skysoft.utils.renderables.verticalOffset
import net.minecraft.client.gui.GuiGraphicsExtractor

class LinearLayoutRenderable private constructor(
    private val children: List<GuiRenderable>,
    private val axis: Axis,
    private val spacing: Int,
    override val horizontalAlign: GuiAlignment.HorizontalAlignment,
    override val verticalAlign: GuiAlignment.VerticalAlignment,
) : GuiRenderable {
    override val width: Int = axis.width(children, spacing)
    override val height: Int = axis.height(children, spacing)

    override fun render(context: GuiGraphicsExtractor) {
        var cursor = 0
        for (child in children) {
            val x = if (axis == Axis.HORIZONTAL) cursor else child.horizontalOffset(width)
            val y = if (axis == Axis.VERTICAL) cursor else child.verticalOffset(height)
            child.renderAt(context, x, y)
            cursor += axis.length(child) + spacing
        }
    }

    private enum class Axis {
        HORIZONTAL,
        VERTICAL,
        ;

        fun width(children: List<GuiRenderable>, spacing: Int): Int = when (this) {
            HORIZONTAL -> children.totalLength(spacing, GuiRenderable::width)
            VERTICAL -> children.maxOfOrNull(GuiRenderable::width) ?: 0
        }

        fun height(children: List<GuiRenderable>, spacing: Int): Int = when (this) {
            HORIZONTAL -> children.maxOfOrNull(GuiRenderable::height) ?: 0
            VERTICAL -> children.totalLength(spacing, GuiRenderable::height)
        }

        fun length(child: GuiRenderable): Int = if (this == HORIZONTAL) child.width else child.height
    }

    companion object {
        fun horizontal(
            children: List<GuiRenderable>,
            spacing: Int = 0,
            horizontalAlign: GuiAlignment.HorizontalAlignment = GuiAlignment.HorizontalAlignment.LEFT,
            verticalAlign: GuiAlignment.VerticalAlignment = GuiAlignment.VerticalAlignment.TOP,
        ): LinearLayoutRenderable =
            LinearLayoutRenderable(children, Axis.HORIZONTAL, spacing, horizontalAlign, verticalAlign)

        fun vertical(
            children: List<GuiRenderable>,
            spacing: Int = 0,
            horizontalAlign: GuiAlignment.HorizontalAlignment = GuiAlignment.HorizontalAlignment.LEFT,
            verticalAlign: GuiAlignment.VerticalAlignment = GuiAlignment.VerticalAlignment.TOP,
        ): LinearLayoutRenderable =
            LinearLayoutRenderable(children, Axis.VERTICAL, spacing, horizontalAlign, verticalAlign)
    }
}

private fun List<GuiRenderable>.totalLength(spacing: Int, size: (GuiRenderable) -> Int): Int =
    sumOf(size) + spacing * (this.size - 1).coerceAtLeast(0)

fun horizontalLayout(
    children: List<GuiRenderable>,
    spacing: Int = 0,
    horizontalAlign: GuiAlignment.HorizontalAlignment = GuiAlignment.HorizontalAlignment.LEFT,
    verticalAlign: GuiAlignment.VerticalAlignment = GuiAlignment.VerticalAlignment.TOP,
): LinearLayoutRenderable = LinearLayoutRenderable.horizontal(children, spacing, horizontalAlign, verticalAlign)

fun verticalLayout(
    children: List<GuiRenderable>,
    spacing: Int = 0,
    horizontalAlign: GuiAlignment.HorizontalAlignment = GuiAlignment.HorizontalAlignment.LEFT,
    verticalAlign: GuiAlignment.VerticalAlignment = GuiAlignment.VerticalAlignment.TOP,
): LinearLayoutRenderable = LinearLayoutRenderable.vertical(children, spacing, horizontalAlign, verticalAlign)
