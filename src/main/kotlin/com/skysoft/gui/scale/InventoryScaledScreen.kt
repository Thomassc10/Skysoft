package com.skysoft.gui.scale

interface InventoryScaledScreen {
    fun usesInventoryScale(): Boolean = true

    fun inventoryScaleLimit(): Int = Int.MAX_VALUE
}
