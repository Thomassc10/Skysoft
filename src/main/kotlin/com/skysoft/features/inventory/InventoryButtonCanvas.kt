package com.skysoft.features.inventory

import com.skysoft.config.InventoryButtonConfig
import com.skysoft.config.InventoryButtonDefaults.PLAYER_INVENTORY_HEIGHT
import com.skysoft.utils.gui.Point
import com.skysoft.utils.gui.Rect

internal data class InventoryButtonCanvas(
    val container: Rect,
    val playerInventory: Boolean,
) {
    private val verticalAnchor: Rect = run {
        if (playerInventory) return@run container
        val height = container.height.coerceAtMost(PLAYER_INVENTORY_HEIGHT)
        val top = container.y + ((container.height - PLAYER_INVENTORY_HEIGHT).coerceAtLeast(0) / 2)
        Rect(container.x, top, container.width, height)
    }

    fun position(button: InventoryButtonConfig): Point {
        val originX = if (button.anchorRight) container.x + container.width else container.x
        val originY = if (button.anchorBottom) verticalAnchor.y + verticalAnchor.height else verticalAnchor.y
        return Point(originX + button.x, originY + button.y)
    }

    fun move(button: InventoryButtonConfig, screenX: Int, screenY: Int) {
        val originX = if (button.anchorRight) container.x + container.width else container.x
        val originY = if (button.anchorBottom) verticalAnchor.y + verticalAnchor.height else verticalAnchor.y
        button.x = screenX - originX
        button.y = screenY - originY
    }

    fun overlapsContainer(buttonBounds: Rect): Boolean = !playerInventory && buttonBounds.intersects(container)
}
