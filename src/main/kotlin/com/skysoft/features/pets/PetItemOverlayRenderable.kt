package com.skysoft.features.pets

import com.skysoft.utils.gui.GuiAlignment
import com.skysoft.utils.renderables.GuiRenderable
import com.skysoft.utils.renderables.renderAt
import net.minecraft.client.gui.GuiGraphicsExtractor

class PetItemOverlayRenderable(
    private val root: GuiRenderable,
    private val item: GuiRenderable,
    private val horizontal: GuiAlignment.HorizontalAlignment,
    private val vertical: GuiAlignment.VerticalAlignment,
) : GuiRenderable {
    override val width: Int = root.width
    override val height: Int = root.height
    override val horizontalAlign = root.horizontalAlign
    override val verticalAlign = root.verticalAlign

    override fun render(context: GuiGraphicsExtractor) {
        root.render(context)
        val anchorX = when (horizontal) {
            GuiAlignment.HorizontalAlignment.LEFT -> 0
            GuiAlignment.HorizontalAlignment.CENTER, GuiAlignment.HorizontalAlignment.DONT_ALIGN -> width / 2
            GuiAlignment.HorizontalAlignment.RIGHT -> width
        }
        val anchorY = when (vertical) {
            GuiAlignment.VerticalAlignment.TOP -> 0
            GuiAlignment.VerticalAlignment.CENTER, GuiAlignment.VerticalAlignment.DONT_ALIGN -> height / 2
            GuiAlignment.VerticalAlignment.BOTTOM -> height
        }
        item.renderAt(context, anchorX - item.width / 2, anchorY - item.height / 2)
    }
}
