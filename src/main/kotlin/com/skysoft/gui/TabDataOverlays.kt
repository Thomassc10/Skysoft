package com.skysoft.gui

import com.skysoft.data.hypixel.HypixelLocationState
import com.skysoft.data.hypixel.TabListApi

object TabDataOverlays {
    val contexts = setOf(
        GuiOverlayContextType.WORLD,
        GuiOverlayContextType.INVENTORY,
        GuiOverlayContextType.STORAGE,
        GuiOverlayContextType.CHAT,
        GuiOverlayContextType.SCREEN,
    )

    val hasStableData: Boolean
        get() = TabListApi.isSkyBlockDataLoaded

    fun canRender(context: GuiOverlayContext): Boolean =
        context.type in contexts && HypixelLocationState.inSkyBlock
}
