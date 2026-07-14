package com.skysoft.gui

import com.skysoft.config.core.HudPosition
import net.minecraft.client.gui.GuiGraphicsExtractor

object HudEditorRegistry {
    private val elements = linkedMapOf<String, HudEditorElement>()

    fun register(element: HudEditorElement) {
        elements[element.id] = element
    }

    fun visibleElements(hasInventoryScreen: Boolean = false): List<HudEditorElement> =
        elements.values.filter {
            isHudEditorElementVisible(it.isVisible(), it.requiresInventoryScreen, hasInventoryScreen)
        }
}

internal fun isHudEditorElementVisible(
    isFeatureVisible: Boolean,
    requiresInventoryScreen: Boolean,
    hasInventoryScreen: Boolean,
): Boolean = isFeatureVisible && (hasInventoryScreen || !requiresInventoryScreen)

interface HudEditorElement {
    val id: String
    val label: String
    val position: HudPosition
    val canScale: Boolean get() = true
    val usesInventoryScale: Boolean get() = false
    val requiresInventoryScreen: Boolean get() = false

    fun width(): Int
    fun height(): Int
    fun isVisible(): Boolean
    fun renderDummy(context: GuiGraphicsExtractor)
    fun renderEditorDummy(context: GuiGraphicsExtractor) = renderDummy(context)
    fun openConfig() = Unit
}
