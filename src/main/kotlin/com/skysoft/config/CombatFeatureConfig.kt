package com.skysoft.config

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CombatFeatureConfig {
    @JvmField
    @field:Expose
    @field:Category(name = "Better Shurikens", desc = "Make Shuriken tags easier to see.")
    val betterShurikens = BetterShurikensConfig()
}

class BetterShurikensConfig {
    @JvmField
    @field:Expose
    @field:ConfigOption(name = "Enabled", desc = "Show Shuriken tags at mobs' feet.")
    @field:ConfigEditorBoolean
    var enabled = true
}
