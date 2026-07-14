package com.skysoft.events.entity

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.Entity

fun interface EntityLoadCallback {
    fun onEntityLoad(entity: Entity)
}

fun interface EntityUnloadCallback {
    fun onEntityUnload(entity: Entity)
}

object EntityLifecycleEvents {
    val LOAD: Event<EntityLoadCallback> = EventFactory.createArrayBacked(EntityLoadCallback::class.java) { listeners ->
        EntityLoadCallback { entity ->
            listeners.forEach { it.onEntityLoad(entity) }
        }
    }

    val UNLOAD: Event<EntityUnloadCallback> = EventFactory.createArrayBacked(EntityUnloadCallback::class.java) { listeners ->
        EntityUnloadCallback { entity ->
            listeners.forEach { it.onEntityUnload(entity) }
        }
    }

    fun register() {
        ClientEntityEvents.ENTITY_LOAD.register { entity, _ -> LOAD.invoker().onEntityLoad(entity) }
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ -> UNLOAD.invoker().onEntityUnload(entity) }
    }
}
