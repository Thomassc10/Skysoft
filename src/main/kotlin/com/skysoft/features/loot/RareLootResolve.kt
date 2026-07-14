package com.skysoft.features.loot

import com.skysoft.features.pets.PetRepository

internal fun RareLootChatDrop.toRareLootDrop(): RareLootDrop {
    val resolvedItemId = bestItemId()
    return RareLootDrop(
        itemId = resolvedItemId,
        displayName = RareLootDisplayNames.resolve(resolvedItemId, displayName),
        amount = amount,
        featureSource = RARE_LOOT_SOURCE,
        context = context,
    )
}

internal fun RareLootChatDrop.bestItemId(): String? {
    val candidates = buildList {
        addAll(itemIdCandidates)
        RareLootItemIds.fromDisplayName(displayName)?.let(::add)
        PetRepository.resolvePetItemOrNull(displayName)?.let(::add)
    }.distinct()
    return candidates.firstOrNull { itemId ->
        RareLootValueResolver.resolve(itemId, amount) != null
    } ?: candidates.firstOrNull()
}

private const val RARE_LOOT_SOURCE = "Rare Loot"
