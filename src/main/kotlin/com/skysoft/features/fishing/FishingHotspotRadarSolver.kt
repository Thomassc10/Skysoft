package com.skysoft.features.fishing

import com.skysoft.utils.WorldVec
import com.skysoft.utils.particle.ParticleEstimateStabilizer
import com.skysoft.utils.particle.ParticlePathEstimator
import com.skysoft.utils.particle.ParticlePathPointResult

internal data class FishingHotspotRadarGuess(
    val location: WorldVec,
    val updatedAtMillis: Long,
)

internal class FishingHotspotRadarSolver(
    private val estimator: ParticlePathEstimator = ParticlePathEstimator(minPoints = MIN_RADAR_PARTICLES),
    private val stabilizer: ParticleEstimateStabilizer = ParticleEstimateStabilizer(),
    private val particleWindowMillis: Long = RADAR_PARTICLE_WINDOW_MILLIS,
) {
    var guess: FishingHotspotRadarGuess? = null
        private set

    private var lastRadarUseMillis: Long? = null

    fun begin(now: Long) {
        estimator.reset()
        stabilizer.reset()
        guess = null
        lastRadarUseMillis = now
    }

    fun addParticle(location: WorldVec, now: Long): FishingHotspotRadarGuess? {
        val radarUse = lastRadarUseMillis ?: return null
        if (now < radarUse || now - radarUse > particleWindowMillis) return null
        if (estimator.addIfUsable(location) != ParticlePathPointResult.ADDED) return null
        if (estimator.count() == 1) stabilizer.reset()
        val estimate = estimator.estimate() ?: return null
        val stableLocation = stabilizer.accept(estimate) ?: return null
        return FishingHotspotRadarGuess(stableLocation, now).also { guess = it }
    }

    fun clear() {
        estimator.reset()
        stabilizer.reset()
        guess = null
        lastRadarUseMillis = null
    }

    private companion object {
        const val MIN_RADAR_PARTICLES = 4
        const val RADAR_PARTICLE_WINDOW_MILLIS = 1_000L
    }
}
