package com.skysoft.config

import com.skysoft.SkysoftMod
import com.skysoft.features.misc.update.ModUpdateChecker
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import net.minecraft.util.Util

class AboutConfig {
    @JvmField
    @field:ConfigOption(
        name = "Updates",
        desc = "Checks Modrinth for a new Skysoft version and opens the manual download page.",
    )
    @field:ConfigEditorUpdate
    val updates = Runnable { ModUpdateChecker.check(force = true) }

    @JvmField
    @field:ConfigOption(
        name = "Skysoft",
        desc = "Heavily inspired by SkyHanni ❤"
    )
    @field:ConfigEditorButton(buttonText = "Source")
    val source = Runnable { openBrowser("https://github.com/akinsoft/Skysoft") }

    @JvmField
    @field:ConfigOption(name = "Credits", desc = "Special thanks and compatibility credits.")
    @field:Accordion
    val credits = CreditsConfig()

    @JvmField
    @field:ConfigOption(name = "Used Software", desc = "Information about used software and licenses.")
    @field:Accordion
    val licenses = LicensesConfig()
}

class CreditsConfig {
    @JvmField
    @field:ConfigOption(name = "SkyHanni", desc = "Skysoft is heavily inspired by SkyHanni.")
    @field:ConfigEditorButton(buttonText = "Source")
    val skyHanni = Runnable { openBrowser("https://github.com/hannibal002/SkyHanni") }

    @JvmField
    @field:ConfigOption(
        name = "Swift",
        desc = "Thanks for permission to match SBO's Diana command style for compatibility.",
    )
    @field:ConfigEditorButton(buttonText = "SBO")
    val swift = Runnable { openBrowser("https://github.com/SkyblockOverhaul/SBO") }
}

class LicensesConfig {
    @JvmField
    @field:ConfigOption(name = "SkyblockRepo", desc = "Item List metadata uses SkyblockRepo under the MIT license.")
    @field:ConfigEditorButton(buttonText = "Source")
    val skyblockRepo = Runnable { openBrowser("https://github.com/SkyblockRepo/Repo") }

    @JvmField
    @field:ConfigOption(name = "SkyBlock API Repo", desc = "Item List data is derived from the NEU item repository under the MIT license.")
    @field:ConfigEditorButton(buttonText = "Source")
    val skyBlockApiRepo = Runnable { openBrowser("https://github.com/SkyblockAPI/Repo") }

    @JvmField
    @field:ConfigOption(
        name = "NEU Repo Data",
        desc = "Item List uses MIT-licensed collection, warp, and texture data from the NEU repository.",
    )
    @field:ConfigEditorButton(buttonText = "Source")
    val neuRepoData = Runnable { openBrowser("https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO") }

    @JvmField
    @field:ConfigOption(
        name = "SkyBlock Wiki",
        desc = "Item List obtain data uses the independent Wiki under CC BY-SA 3.0.",
    )
    @field:ConfigEditorButton(buttonText = "Source")
    val skyBlockWiki = Runnable { openBrowser("https://hypixelskyblock.minecraft.wiki/") }

    @JvmField
    @field:ConfigOption(
        name = "Official Wiki",
        desc = "Item List event availability uses facts from the official Hypixel Wiki.",
    )
    @field:ConfigEditorButton(buttonText = "Source")
    val officialWiki = Runnable { openBrowser("https://wiki.hypixel.net/") }

    @JvmField
    @field:ConfigOption(name = "MoulConfig", desc = "Skysoft uses a modified MoulConfig fork under the LGPL 3.0 license or later.")
    @field:ConfigEditorButton(buttonText = "Source")
    val moulConfig = Runnable { openBrowser("https://github.com/akinsoft/MoulConfig") }

    @JvmField
    @field:ConfigOption(name = "Fabric Loader", desc = "Fabric Loader is available under the Apache-2.0 license.")
    @field:ConfigEditorButton(buttonText = "Source")
    val fabricLoader = Runnable { openBrowser("https://github.com/FabricMC/fabric-loader") }

    @JvmField
    @field:ConfigOption(name = "Fabric API", desc = "Fabric API is available under the Apache-2.0 license.")
    @field:ConfigEditorButton(buttonText = "Source")
    val fabricApi = Runnable { openBrowser("https://github.com/FabricMC/fabric") }

    @JvmField
    @field:ConfigOption(name = "Hypixel Mod API", desc = "Hypixel Mod API is available from Hypixel.")
    @field:ConfigEditorButton(buttonText = "Source")
    val hypixelModApi = Runnable { openBrowser("https://github.com/HypixelDev/ModAPI") }
}

private fun openBrowser(url: String) {
    try {
        Util.getPlatform().openUri(url)
    } catch (e: Exception) {
        SkysoftMod.LOGGER.warn("Failed to open browser for $url", e)
    }
}
