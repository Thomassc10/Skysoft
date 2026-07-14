package com.skysoft.features.misc.blockoverlay

import com.skysoft.config.BlockOverlayCombination
import com.skysoft.config.BlockOverlayCondition
import com.skysoft.config.BlockOverlayConditionKind

object BlockOverlayItemCatalogue {
    private val itemNames = linkedMapOf<String, String>()

    fun startSession(combinations: List<BlockOverlayCombination>) {
        itemNames.clear()
        rememberActiveItems(combinations)
    }

    fun rememberActiveItems(combinations: List<BlockOverlayCombination>) {
        combinations.asSequence()
            .flatMap { it.conditions.asSequence() }
            .filter { it.kind == BlockOverlayConditionKind.ITEM }
            .forEach { condition -> itemNames.putIfAbsent(condition.value, condition.displayName) }
    }

    private fun registerItem(itemId: String, displayName: String): BlockOverlayItemAddResult =
        if (itemNames.putIfAbsent(itemId, displayName) == null) {
            BlockOverlayItemAddResult.ADDED
        } else {
            BlockOverlayItemAddResult.ALREADY_AVAILABLE
        }

    fun addActiveItem(
        combinations: MutableList<BlockOverlayCombination>,
        itemId: String,
        displayName: String,
    ): BlockOverlayItemAddResult {
        val result = registerItem(itemId, displayName)
        if (result == BlockOverlayItemAddResult.ALREADY_AVAILABLE) return result
        combinations.add(
            0,
            BlockOverlayCombination(
                mutableListOf(BlockOverlayCondition(BlockOverlayConditionKind.ITEM, itemId, displayName)),
            ),
        )
        return BlockOverlayItemAddResult.ADDED
    }

    fun contains(itemId: String): Boolean = itemId in itemNames

    fun conditions(): List<BlockOverlayCondition> = itemNames.map { (itemId, displayName) ->
        BlockOverlayCondition(BlockOverlayConditionKind.ITEM, itemId, displayName)
    }
}

enum class BlockOverlayItemAddResult {
    ADDED,
    ALREADY_AVAILABLE,
}
