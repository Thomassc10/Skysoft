package com.skysoft.features.fishing

import com.skysoft.events.particle.ClientParticleEvent
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.ParticleTypes
import kotlin.math.abs

internal object FishingHotspotRadarParticleClassifier {
    fun isTrail(event: ClientParticleEvent): Boolean =
        isTrail(event.type, event.count, event.speed)

    fun isTrail(type: ParticleType<*>, count: Int, speed: Float): Boolean =
        type == ParticleTypes.FLAME && count == TRAIL_PARTICLE_COUNT && abs(speed) <= SPEED_TOLERANCE

    private const val TRAIL_PARTICLE_COUNT = 1
    private const val SPEED_TOLERANCE = 0.0001f
}
