package com.skysoft

import com.skysoft.config.SkysoftConfigGui
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen

class SkysoftModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> =
        ConfigScreenFactory { parent -> SkysoftConfigGui.createScreen(parent) }
}
