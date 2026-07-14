package com.skysoft.features.inventory

import com.skysoft.config.SkysoftConfigGui
import com.skysoft.data.hypixel.HypixelLocationState
import com.skysoft.data.skyblock.SkyBlockMenuItem.isSkyBlockMenu
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack

object SkyBlockMenuInventoryDropFix {
    @JvmStatic
    fun beginContainerThrow(
        player: Player,
        slotId: Int,
        action: ContainerInput,
    ): InventoryDropSelectionGuard? {
        if (action != ContainerInput.THROW) return null
        val localPlayer = player as? LocalPlayer
        val sourceItem = localPlayer?.containerMenu?.slots?.getOrNull(slotId)?.item
        val rejectionReason = rejectionReason(localPlayer, sourceItem)
        if (rejectionReason != null) return null
        val activePlayer = requireNotNull(localPlayer)

        val inventory = activePlayer.inventory
        val originalSlot = inventory.selectedSlot
        val temporarySlot = temporaryHotbarSlot(activePlayer, originalSlot)
            ?: return null
        activePlayer.connection.send(ServerboundSetCarriedItemPacket(temporarySlot))
        return InventoryDropSelectionGuard(activePlayer, originalSlot)
    }

    @JvmStatic
    fun finishContainerThrow(guard: InventoryDropSelectionGuard?) {
        guard ?: return
        guard.player.connection.send(ServerboundSetCarriedItemPacket(guard.originalSlot))
    }

    private fun temporaryHotbarSlot(player: LocalPlayer, originalSlot: Int): Int? {
        val inventory = player.inventory
        var firstNonMenuSlot: Int? = null
        for (offset in 1 until HOTBAR_SIZE) {
            val candidate = (originalSlot + offset) % HOTBAR_SIZE
            val item = inventory.getItem(candidate)
            if (item.isEmpty) return candidate
            if (firstNonMenuSlot == null && !item.isSkyBlockMenu()) firstNonMenuSlot = candidate
        }
        return firstNonMenuSlot
    }

    private fun rejectionReason(player: LocalPlayer?, sourceItem: ItemStack?): String? =
        when {
            player == null -> "player is not local"
            !config.preventSkyBlockMenuOpeningOnInventoryDrop -> "disabled"
            !HypixelLocationState.inSkyBlock -> "not in SkyBlock"
            !player.mainHandItem.isSkyBlockMenu() -> "held item is not the SkyBlock Menu"
            sourceItem == null || sourceItem.isEmpty -> "source slot is empty"
            sourceItem.isSkyBlockMenu() -> "source item is the SkyBlock Menu"
            else -> null
        }

    private val config
        get() = SkysoftConfigGui.config().fixes

    private const val HOTBAR_SIZE = 9
}

class InventoryDropSelectionGuard internal constructor(
    internal val player: LocalPlayer,
    internal val originalSlot: Int,
)
