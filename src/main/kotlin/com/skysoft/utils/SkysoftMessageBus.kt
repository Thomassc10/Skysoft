package com.skysoft.utils

import com.skysoft.SkysoftMod
import com.skysoft.utils.TextUtilities.cleanSkyBlockText
import com.skysoft.utils.TextUtilities.formattedText
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component

object SkysoftMessageBus {
    private var listeners: List<(SkysoftMessage) -> SkysoftMessageVisibility> = emptyList()
    private var gameModifiers: List<(SkysoftMessage) -> Component> = emptyList()
    private var registered = false

    fun register() {
        if (registered) return
        registered = true

        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            dispatch(SkysoftMessage(message, SkysoftMessageSource.CHAT)).allowsMessage
        }
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            dispatch(SkysoftMessage(message, SkysoftMessageSource.GAME, overlay)).allowsMessage
        }
        ClientReceiveMessageEvents.MODIFY_GAME.register { message, overlay ->
            modifyGameMessage(SkysoftMessage(message, SkysoftMessageSource.GAME, overlay))
        }
    }

    fun onMessage(listener: (SkysoftMessage) -> SkysoftMessageVisibility) {
        register()
        listeners += listener
    }

    fun onGameModify(modifier: (SkysoftMessage) -> Component) {
        register()
        gameModifiers += modifier
    }

    private fun dispatch(message: SkysoftMessage): SkysoftMessageVisibility =
        listeners.fold(SkysoftMessageVisibility.SHOW) { result, listener ->
            try {
                listener(message).combine(result)
            } catch (exception: Exception) {
                SkysoftMod.LOGGER.error("Skysoft message listener failed", exception)
                result
            }
        }

    private fun modifyGameMessage(message: SkysoftMessage): Component =
        gameModifiers.fold(message.component) { component, modifier ->
            try {
                modifier(SkysoftMessage(component, message.source, message.overlay))
            } catch (exception: Exception) {
                SkysoftMod.LOGGER.error("Skysoft game message modifier failed", exception)
                component
            }
        }

    private fun SkysoftMessageVisibility.combine(previous: SkysoftMessageVisibility): SkysoftMessageVisibility =
        if (this == SkysoftMessageVisibility.HIDE) this else previous
}

class SkysoftMessage(
    val component: Component,
    val source: SkysoftMessageSource,
    val overlay: Boolean = false,
) {
    val plainText: String = component.string
    val cleanText: String by lazy { component.cleanSkyBlockText() }
    val formattedText: String by lazy { component.formattedText() }
    val cleanFormattedText: String by lazy { formattedText.cleanSkyBlockText() }
}

enum class SkysoftMessageSource {
    CHAT,
    GAME,
}

enum class SkysoftMessageVisibility {
    SHOW,
    HIDE,
    ;

    val allowsMessage: Boolean
        get() = this == SHOW
}
