package com.skysoft.utils.particle

import com.skysoft.utils.WorldVec

internal class ParticleEstimateStabilizer(
    private val maxMovement: Double = DEFAULT_MAX_MOVEMENT,
    private val requiredStableEstimates: Int = DEFAULT_REQUIRED_STABLE_ESTIMATES,
) {
    private var lastEstimate: WorldVec? = null
    private var stableEstimates = 0

    init {
        require(maxMovement >= 0.0) { "Particle estimate movement limit cannot be negative." }
        require(requiredStableEstimates > 0) { "Particle estimate stabilizer needs at least one estimate." }
    }

    fun reset() {
        lastEstimate = null
        stableEstimates = 0
    }

    fun accept(location: WorldVec): WorldVec? {
        val block = location.roundToBlock()
        val last = lastEstimate
        stableEstimates = if (last != null && last.distance(block) <= maxMovement) {
            stableEstimates + 1
        } else {
            1
        }
        lastEstimate = block
        return block.takeIf { stableEstimates >= requiredStableEstimates }
    }

    private companion object {
        const val DEFAULT_MAX_MOVEMENT = 3.0
        const val DEFAULT_REQUIRED_STABLE_ESTIMATES = 2
    }
}
