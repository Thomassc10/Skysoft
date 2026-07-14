package com.skysoft.features.chat

import com.skysoft.config.SkysoftConfigGui

object ChatMotionSettings {
    private val config get() = SkysoftConfigGui.config().chat.smoothChat

    @JvmStatic
    fun isMessageMotionEnabled(): Boolean = config.animateMessages

    @JvmStatic
    fun newMessageDurationMillis(): Int = config.settings.messageAnimationDuration

    @JvmStatic
    fun isMessageIndicatorHidden(): Boolean = config.details.hideMessageIndicator

    @JvmStatic
    fun isInputMotionEnabled(): Boolean = config.animateChatOpen

    @JvmStatic
    fun chatInputDurationMillis(): Int = config.settings.chatOpenAnimationDuration
}
