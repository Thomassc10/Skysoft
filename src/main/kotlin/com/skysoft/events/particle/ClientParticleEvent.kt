package com.skysoft.events.particle

import com.skysoft.utils.WorldVec
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.core.particles.ParticleType

class ClientParticleEvent(
    val type: ParticleType<*>,
    val location: WorldVec,
    val count: Int,
    val speed: Float,
    val offset: WorldVec,
    val longDistance: Boolean,
)

fun interface ReceiveParticleCallback {
    fun shouldCancelParticle(event: ClientParticleEvent): Boolean
}

object ClientParticleEvents {
    val EVENT: Event<ReceiveParticleCallback> =
        EventFactory.createArrayBacked(ReceiveParticleCallback::class.java, ::receiveParticleInvoker)
}

internal fun receiveParticleInvoker(listeners: Array<ReceiveParticleCallback>): ReceiveParticleCallback =
    ReceiveParticleCallback { event ->
        var cancelled = false
        listeners.forEach { listener ->
            cancelled = listener.shouldCancelParticle(event) || cancelled
        }
        cancelled
    }
