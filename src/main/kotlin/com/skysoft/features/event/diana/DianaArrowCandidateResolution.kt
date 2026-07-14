package com.skysoft.features.event.diana

internal fun DianaArrowRay.resolveCandidates(bounds: DianaArrowBounds): List<ResolvedArrowCandidate> {
    val projection = DianaArrowProjector.projectDetailed(this, bounds)
    if (projection.candidates.isEmpty()) return emptyList()
    return DianaArrowCandidateResolver.resolve(this, projection.scoredCandidates)
}
