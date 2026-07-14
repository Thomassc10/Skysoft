package com.skysoft.config

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ChatFeatureConfig {
    @JvmField
    @field:Expose
    @field:Category(name = "Smooth Chat", desc = "Chat animation and visual settings.")
    val smoothChat = SmoothChatConfig()

    fun repairLoadedValues() {
        smoothChat.repairLoadedValues()
    }

    class SmoothChatConfig {
        @JvmField
        @field:Expose
        @field:ConfigOption(name = "Animate Messages", desc = "Slide new chat messages into place.")
        @field:ConfigEditorBoolean
        var animateMessages = true

        @JvmField
        @field:Expose
        @field:ConfigOption(name = "Animate Chat Open", desc = "Slide the chat input bar into place when chat opens.")
        @field:ConfigEditorBoolean
        var animateChatOpen = true

        @JvmField
        @field:Expose
        @field:ConfigOption(name = "Settings", desc = "Chat animation timing settings.")
        @field:Accordion
        val settings = SmoothChatSettingsConfig()

        @JvmField
        @field:Expose
        @field:ConfigOption(name = "Details", desc = "Additional chat visual settings.")
        @field:Accordion
        val details = SmoothChatDetailsConfig()

        fun repairLoadedValues() {
            settings.repairLoadedValues()
        }
    }

    class SmoothChatSettingsConfig {
        @JvmField
        @field:Expose
        @field:ConfigOption(name = "Message Animation Duration", desc = "Duration of the new-message animation in milliseconds.")
        @field:ConfigEditorSlider(minValue = 10f, maxValue = 300f, minStep = 1f)
        var messageAnimationDuration = DEFAULT_MESSAGE_ANIMATION_DURATION

        @JvmField
        @field:Expose
        @field:ConfigOption(name = "Chat Open Animation Duration", desc = "Duration of the chat-open animation in milliseconds.")
        @field:ConfigEditorSlider(minValue = 10f, maxValue = 700f, minStep = 1f)
        var chatOpenAnimationDuration = DEFAULT_CHAT_OPEN_ANIMATION_DURATION

        fun repairLoadedValues() {
            messageAnimationDuration = messageAnimationDuration.coerceIn(
                MIN_MESSAGE_ANIMATION_DURATION,
                MAX_MESSAGE_ANIMATION_DURATION,
            )
            chatOpenAnimationDuration = chatOpenAnimationDuration.coerceIn(
                MIN_CHAT_OPEN_ANIMATION_DURATION,
                MAX_CHAT_OPEN_ANIMATION_DURATION,
            )
        }
    }

    class SmoothChatDetailsConfig {
        @JvmField
        @field:Expose
        @field:ConfigOption(name = "Hide Message Indicator", desc = "Hide the message indicator line to the left of chat messages.")
        @field:ConfigEditorBoolean
        var hideMessageIndicator = true
    }
}

const val MIN_MESSAGE_ANIMATION_DURATION = 10
const val MAX_MESSAGE_ANIMATION_DURATION = 300
const val DEFAULT_MESSAGE_ANIMATION_DURATION = 150
const val MIN_CHAT_OPEN_ANIMATION_DURATION = 10
const val MAX_CHAT_OPEN_ANIMATION_DURATION = 700
const val DEFAULT_CHAT_OPEN_ANIMATION_DURATION = 170
