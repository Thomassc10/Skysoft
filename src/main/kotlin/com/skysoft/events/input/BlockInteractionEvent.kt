package com.skysoft.events.input

import com.skysoft.data.InteractionClick
import com.skysoft.utils.WorldVec
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.item.ItemStack

class BlockInteractionEvent(
    val clickType: InteractionClick,
    val itemInHand: ItemStack?,
    val position: WorldVec,
)

fun interface BlockClickCallback {
    fun shouldCancelBlockClick(event: BlockInteractionEvent): Boolean
}

object BlockInteractionEvents {
    val EVENT: Event<BlockClickCallback> = EventFactory.createArrayBacked(BlockClickCallback::class.java) { listeners ->
        BlockClickCallback { event ->
            listeners.any { it.shouldCancelBlockClick(event) }
        }
    }
}
