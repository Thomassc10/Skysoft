package com.skysoft.events.entity

import com.skysoft.data.InteractionClick
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.Entity

class EntityInteractionEvent(
    val clickType: InteractionClick,
    val action: ActionType,
    val clickedEntity: Entity,
) {
    enum class ActionType {
        INTERACT,
        ATTACK,
        INTERACT_AT,
    }
}

fun interface EntityClickCallback {
    fun shouldCancelEntityClick(event: EntityInteractionEvent): Boolean
}

object EntityInteractionEvents {
    val EVENT: Event<EntityClickCallback> = EventFactory.createArrayBacked(EntityClickCallback::class.java) { listeners ->
        EntityClickCallback { event ->
            listeners.any { it.shouldCancelEntityClick(event) }
        }
    }
}
