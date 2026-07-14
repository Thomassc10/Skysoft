package com.skysoft.features.inventory

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot
import java.util.IdentityHashMap

private val originalSlotPositions = IdentityHashMap<AbstractContainerScreen<*>, MutableMap<Slot, SlotPosition>>()

internal fun moveStorageOverlaySlot(screen: AbstractContainerScreen<*>, slot: Slot, x: Int, y: Int) {
    originalSlotPositions.getOrPut(screen) { IdentityHashMap() }
        .putIfAbsent(slot, SlotPosition(slot.x, slot.y))
    setSlotPosition(slot, x, y)
}

internal fun restoreStorageOverlaySlots(screen: AbstractContainerScreen<*>? = null) {
    if (screen != null) {
        originalSlotPositions.remove(screen)?.let(::restoreSlots)
        return
    }

    val slotsByScreen = originalSlotPositions.values.toList()
    originalSlotPositions.clear()
    slotsByScreen.forEach(::restoreSlots)
}

private fun restoreSlots(slots: Map<Slot, SlotPosition>) {
    slots.forEach { (slot, position) -> setSlotPosition(slot, position.x, position.y) }
}

private data class SlotPosition(val x: Int, val y: Int)
