package com.skysoft.config

import com.skysoft.features.misc.autosprint.AutoSprint
import com.skysoft.features.misc.conditions.FeatureConditions

object AutoSprintCombinationsProvider : FeatureCombinationsProvider() {
    override fun getChoices(): List<FeatureCondition> =
        FeatureConditions.builtInConditions() + AutoSprint.itemConditions()

    override fun onChanged() = AutoSprint.markConditionsChanged()
}
