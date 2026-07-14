package com.skysoft.config

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FixesConfig {
    @JvmField
    @field:Expose
    @field:ConfigOption(
        name = "Menu Drop Fix",
        desc = "Prevent the SkyBlock Menu from opening when dropping hovered inventory items.",
    )
    @field:ConfigEditorBoolean
    var preventSkyBlockMenuOpeningOnInventoryDrop = true

    @JvmField
    @field:Expose
    @field:ConfigOption(
        name = "Hide Glitch Mobs",
        desc = "Hide nametagless rare mob player models left behind by Hypixel.",
    )
    @field:ConfigEditorBoolean
    var hideGlitchMobs = true

    @JvmField
    @field:Expose
    @field:ConfigOption(
        name = "Hide Bugged Nameplates",
        desc = "Hide bugged floating nameplates left behind by Hypixel.",
    )
    @field:ConfigEditorBoolean
    var hideBuggedNameplates = true

    @JvmField
    @field:Expose
    @field:ConfigOption(
        name = "Player Head Skin Fix",
        desc = "Stops custom player heads from flashing the default player face while their real skin loads.",
    )
    @field:ConfigEditorBoolean
    var playerHeadSkinFix = true
}
