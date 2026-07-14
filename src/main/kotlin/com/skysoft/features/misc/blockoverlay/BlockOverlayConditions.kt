package com.skysoft.features.misc.blockoverlay

import com.skysoft.config.BlockOverlayCombination
import com.skysoft.config.BlockOverlayCondition
import com.skysoft.config.BlockOverlayConditionKind
import com.skysoft.data.SkyBlockIsland
import com.skysoft.data.skyblock.SkyBlockEvent
import java.util.Locale

data class BlockOverlayConditionContext(
    val isInSkyBlock: Boolean,
    val island: SkyBlockIsland?,
    val activeEvents: Set<SkyBlockEvent>,
    val heldItemId: String?,
)

object BlockOverlayConditions {
    fun matches(combinations: List<BlockOverlayCombination>, context: BlockOverlayConditionContext): Boolean =
        combinations.isEmpty() || combinations.any { combination ->
            combination.conditions.isNotEmpty() && combination.conditions.all { it.matches(context) }
        }

    fun builtInConditions(): List<BlockOverlayCondition> = buildList {
        SkyBlockEvent.entries.forEach { event ->
            add(BlockOverlayCondition(BlockOverlayConditionKind.EVENT, event.name, "Event: ${event.displayName}"))
        }
        SkyBlockIsland.entries.forEach { island ->
            add(
                BlockOverlayCondition(
                    BlockOverlayConditionKind.ISLAND,
                    island.name,
                    "On Island: ${island.displayName}",
                ),
            )
        }
    }

    fun BlockOverlayCondition.key(): String = "${kind.name}:${value.normalizedValue()}"

    fun BlockOverlayCondition.copyCondition(): BlockOverlayCondition =
        BlockOverlayCondition(kind, value, displayName)

    fun BlockOverlayCondition.selectedDisplayName(): String = when (kind) {
        BlockOverlayConditionKind.EVENT -> displayName.removePrefix("Event: ")
        BlockOverlayConditionKind.ISLAND -> displayName.removePrefix("On Island: ")
        BlockOverlayConditionKind.ITEM -> displayName.removePrefix("Holding Item: ")
    }

    private fun BlockOverlayCondition.matches(context: BlockOverlayConditionContext): Boolean = when (kind) {
        BlockOverlayConditionKind.EVENT ->
            context.isInSkyBlock && runCatching { SkyBlockEvent.valueOf(value) }.getOrNull() in context.activeEvents
        BlockOverlayConditionKind.ISLAND ->
            SkyBlockIsland.getByConditionValue(value) == context.island
        BlockOverlayConditionKind.ITEM ->
            context.heldItemId?.normalizedValue() == value.normalizedValue()
    }

    private fun String.normalizedValue(): String = trim().uppercase(Locale.US)

}
