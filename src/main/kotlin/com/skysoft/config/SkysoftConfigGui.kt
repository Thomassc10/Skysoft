package com.skysoft.config

import com.skysoft.utils.MinecraftClient
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object SkysoftConfigGui {
    private val config = SkysoftConfig.load()
    private var editor: MoulConfigEditor<SkysoftConfig>? = null

    fun open(search: String? = null) {
        val currentEditor = editor()
        val query = configSearchQuery(search)
        val matchingCategory = currentEditor.allCategories.values
            .firstOrNull { it.displayName.text.equals(query, ignoreCase = true) }
        if (matchingCategory != null) {
            currentEditor.search("")
            currentEditor.setSelectedCategory(matchingCategory)
        } else {
            currentEditor.search(query)
            selectExactOption(currentEditor, query)
        }
        MinecraftClient.setScreen(createScreen(null))
    }

    fun createScreen(parent: Screen?): Screen =
        object : MoulConfigScreenComponent(
            Component.empty(),
            GuiContext(GuiElementComponent(editor())),
            parent,
        ) {
            override fun removed() {
                super.removed()
                config.saveNow()
            }
        }

    fun config(): SkysoftConfig = config

    private fun editor(): MoulConfigEditor<SkysoftConfig> {
        val currentEditor = editor
        if (currentEditor != null) {
            return currentEditor
        }

        return SkysoftMoulConfigGuis.createEditor(config).also { editor = it }
    }
}

internal fun configSearchQuery(search: String?): String = search.orEmpty()

private fun selectExactOption(editor: MoulConfigEditor<SkysoftConfig>, query: String) {
    if (query.isBlank()) return
    val matchingOption = editor.allOptions.firstOrNull { it.name.text.equals(query, ignoreCase = true) }
    if (matchingOption != null) {
        editor.goToOption(matchingOption)
        return
    }
}
