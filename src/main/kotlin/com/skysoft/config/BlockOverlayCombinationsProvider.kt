package com.skysoft.config

import com.skysoft.features.misc.blockoverlay.BlockOverlayConditions
import com.skysoft.features.misc.blockoverlay.BlockOverlayConditions.copyCondition
import com.skysoft.features.misc.blockoverlay.BlockOverlayConditions.key
import com.skysoft.features.misc.blockoverlay.BlockOverlayConditions.selectedDisplayName
import com.skysoft.features.misc.blockoverlay.BlockOverlayItemCatalogue
import com.skysoft.features.misc.blockoverlay.BlockOverlayRules
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.editors.ConfigEditorCombinationsProvider

object BlockOverlayCombinationsProvider :
    ConfigEditorCombinationsProvider<BlockOverlayCombination, BlockOverlayCondition>(
        BlockOverlayCombination::class.java,
        BlockOverlayCondition::class.java,
    ) {
    override fun getChoices(): List<BlockOverlayCondition> =
        BlockOverlayConditions.builtInConditions() + BlockOverlayItemCatalogue.conditions()

    override fun getEntries(combination: BlockOverlayCombination): MutableList<BlockOverlayCondition> =
        combination.conditions

    override fun createCombination(firstChoice: BlockOverlayCondition): BlockOverlayCombination =
        BlockOverlayCombination(mutableListOf(firstChoice))

    override fun getChoiceLabel(choice: BlockOverlayCondition): StructuredText =
        StructuredText.of(choice.displayName)

    override fun getSelectedLabel(choice: BlockOverlayCondition): StructuredText =
        StructuredText.of(choice.selectedDisplayName())

    override fun getChoiceId(choice: BlockOverlayCondition): Any = choice.key()

    override fun getChoiceGroup(choice: BlockOverlayCondition): Any = choice.kind

    override fun copyChoice(choice: BlockOverlayCondition): BlockOverlayCondition = choice.copyCondition()

    override fun onChanged() = BlockOverlayRules.markChanged()
}
