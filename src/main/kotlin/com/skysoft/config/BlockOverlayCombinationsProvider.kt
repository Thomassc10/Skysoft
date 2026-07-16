package com.skysoft.config

import com.skysoft.features.misc.blockoverlay.BlockOverlay
import com.skysoft.features.misc.conditions.FeatureConditions

object BlockOverlayCombinationsProvider : FeatureCombinationsProvider() {
    override fun getChoices(): List<FeatureCondition> =
        FeatureConditions.builtInConditions() + BlockOverlay.itemConditions()

    override fun onChanged() = BlockOverlay.markConditionsChanged()
}
