package com.skysoft.gui.scale;

public interface InventoryScaledScreen {
    default boolean usesInventoryScale() {
        return true;
    }

    default int inventoryScaleLimit() {
        return Integer.MAX_VALUE;
    }
}
