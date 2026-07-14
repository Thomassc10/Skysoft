package com.skysoft.features.pets

import com.skysoft.data.StoredPetData
import com.skysoft.features.pets.ActivePetTracker.PetDataAssertionSource
import com.skysoft.utils.ChangeResult
import com.skysoft.utils.MinecraftClient
import com.skysoft.utils.ElapsedTimeMark
import com.skysoft.utils.input.InputUtilities
import java.util.UUID
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

internal object PetStorageClickHandler {
    fun onSlotClick(slot: Slot?, slotId: Int, clickedButton: Int) {
        val screen = MinecraftClient.screen()
        val inventoryName = screen?.title?.string
        if (!PetStoragePetItems.isMainPetMenuName(inventoryName)) return
        if (!PetStoragePetItems.isPetStackLocation(slotId)) return
        val clickedItem = slot?.item?.takeUnless { it.isEmpty } ?: return
        val clickedPetData = PetStoragePetItems.toClickedPetDataOrNull(clickedItem) ?: return
        val clickedPetUuid = clickedPetData.uuid
        val currentPetUuid = PetStorageService.petStorage.currentPetUuid
        val change = when (clickedButton) {
            1 -> removeClickedPet(clickedPetUuid, currentPetUuid)
            0 -> selectClickedPet(clickedItem, clickedPetData, clickedPetUuid, currentPetUuid)
            else -> ChangeResult.UNCHANGED
        }
        if (change == ChangeResult.CHANGED) PetStorageService.markDirty()
    }

    private fun removeClickedPet(clickedPetUuid: UUID?, currentPetUuid: UUID?): ChangeResult {
        clickedPetUuid ?: return ChangeResult.UNCHANGED
        val removedPet = PetStorageService.petStorage.pets.removeIf { it.uuid == clickedPetUuid }
        var removedExpShareReference = false
        PetStorageService.petStorage.expSharePets.replaceAll { uuid ->
            if (uuid == clickedPetUuid) {
                removedExpShareReference = true
                null
            } else {
                uuid
            }
        }
        val clearedCurrentPet = currentPetUuid == clickedPetUuid
        if (clearedCurrentPet) ActivePetTracker.clearCurrentPet()
        return ChangeResult.from(removedPet || removedExpShareReference || clearedCurrentPet)
    }

    private fun selectClickedPet(
        clickedItem: ItemStack,
        clickedPetData: StoredPetData,
        clickedPetUuid: UUID?,
        currentPetUuid: UUID?,
    ): ChangeResult {
        if (isShiftKeyDown()) return ChangeResult.UNCHANGED
        PetStorageService.lastExactPetMenuClick = ElapsedTimeMark.now()
        if (PetStoragePetItems.isCurrentPetStack(clickedItem) || currentPetUuid == clickedPetUuid) {
            ActivePetTracker.clearCurrentPet()
        } else {
            if (clickedPetUuid != null) {
                PetStorageService.petStorage.pets.addOrReplace(clickedPetData) { it.uuid == clickedPetUuid }
            }
            ActivePetTracker.assertFoundCurrentData(clickedPetData, PetDataAssertionSource.MENU)
        }
        return ChangeResult.CHANGED
    }

    private fun isShiftKeyDown(): Boolean = InputUtilities.isShiftDown()
}
