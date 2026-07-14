package com.skysoft.features.event.diana

import com.skysoft.utils.WorldVec

internal enum class DianaWarpPoint(
    val displayName: String,
    val command: String,
    val location: WorldVec,
    val extraBlocks: Double,
) {
    HUB("Hub", "hub", WorldVec(0.5, 77.0, -0.5), 0.0),
    CASTLE("Castle", "castle", WorldVec(-250.0, 130.0, 45.0), 10.0),
    DARK_AUCTION("Dark Auction", "da", WorldVec(91.5, 75.0, 173.5), 2.0),
    MUSEUM("Museum", "museum", WorldVec(29.0, 72.0, 1.0), 2.0),
    STONKS("Stonks", "stonks", WorldVec(-36.5, 70.0, -81.5), 5.0),
}

internal data class DianaWarpSuggestion(
    val point: DianaWarpPoint,
    val savings: Double,
)

internal object DianaWarpSelector {
    fun bestWarp(
        target: WorldVec,
        playerLocation: WorldVec,
        minSavings: Double,
        disabledCommands: Set<String> = emptySet(),
    ): DianaWarpSuggestion? {
        val playerDistance = playerLocation.distance(target)
        return DianaWarpPoint.entries
            .asSequence()
            .filter { it.command !in disabledCommands }
            .map { warp ->
                val routeDistance = warp.location.distance(target) + warp.extraBlocks
                DianaWarpSuggestion(warp, playerDistance - routeDistance)
            }
            .filter { it.savings >= minSavings }
            .maxByOrNull { it.savings }
    }
}
