package com.skysoft.events.entity

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.syncher.SynchedEntityData

class ClientEntityMetadataEvent(
    val entityId: Int,
    val packedItems: List<SynchedEntityData.DataValue<*>>,
)

fun interface ReceiveEntityMetadataCallback {
    fun onReceiveEntityMetadata(event: ClientEntityMetadataEvent)
}

object ClientEntityMetadataEvents {
    val EVENT: Event<ReceiveEntityMetadataCallback> =
        EventFactory.createArrayBacked(ReceiveEntityMetadataCallback::class.java) { listeners ->
            ReceiveEntityMetadataCallback { event ->
                listeners.forEach { it.onReceiveEntityMetadata(event) }
            }
        }
}
