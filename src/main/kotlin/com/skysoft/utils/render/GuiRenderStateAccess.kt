package com.skysoft.utils.render

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.state.gui.GuiRenderState
import java.lang.reflect.Field

internal object GuiRenderStateAccess {
    fun get(context: GuiGraphicsExtractor): GuiRenderState =
        field.get(context) as GuiRenderState

    private val field: Field by lazy {
        GuiGraphicsExtractor::class.java.declaredFields
            .single { GuiRenderState::class.java.isAssignableFrom(it.type) }
            .apply { isAccessible = true }
    }
}
