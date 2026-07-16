package com.skysoft.mixin

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.llamalad7.mixinextras.sugar.Local
import com.skysoft.features.chat.ChatMotionProfile
import com.skysoft.features.chat.ChatMotionSettings
import com.skysoft.features.event.diana.DianaSphinxAnswerHighlighter
import com.skysoft.utils.animation.AnimationClock
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.client.multiplayer.chat.GuiMessageSource
import net.minecraft.client.multiplayer.chat.GuiMessageTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MessageSignature
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.ModifyVariable
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ChatComponent::class)
abstract class ChatComponentMixin {
    @field:Shadow
    private var chatScrollbarPos = 0

    @field:Unique
    private val skysoftMessageArrival = AnimationClock()

    @Shadow
    private fun getLineHeight(): Int = throw AssertionError()

    @WrapOperation(
        method = [
            "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;" +
                "Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/" +
                "ChatComponent\$DisplayMode;Z)V",
        ],
        at = [
            At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/components/ChatComponent;" +
                    "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent\$ChatGraphicsAccess;" +
                    "IILnet/minecraft/client/gui/components/ChatComponent\$DisplayMode;)V",
            ),
        ],
    )
    protected fun skysoftAnimateNewMessages(
        chat: ChatComponent,
        graphicsAccess: ChatComponent.ChatGraphicsAccess,
        screenHeight: Int,
        ticks: Int,
        displayMode: ChatComponent.DisplayMode,
        original: Operation<Void>,
        @Local(argsOnly = true) graphics: GuiGraphicsExtractor,
    ) {
        val displacement = skysoftNewMessageOffset()
        if (displacement == 0.0f) {
            original.call(chat, graphicsAccess, screenHeight, ticks, displayMode)
            return
        }

        graphics.pose().pushMatrix()
        try {
            graphics.pose().translate(0.0f, displacement)
            original.call(chat, graphicsAccess, screenHeight, ticks, displayMode)
        } finally {
            graphics.pose().popMatrix()
        }
    }

    @Inject(
        method = [
            "addMessage(Lnet/minecraft/network/chat/Component;" +
                "Lnet/minecraft/network/chat/MessageSignature;" +
                "Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;" +
                "Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
        ],
        at = [At("TAIL")],
    )
    protected fun skysoftRecordMessageTime(
        contents: Component,
        signature: MessageSignature?,
        source: GuiMessageSource,
        tag: GuiMessageTag?,
        ci: CallbackInfo,
    ) {
        skysoftMessageArrival.restart()
    }

    @ModifyVariable(
        method = [
            "addMessage(Lnet/minecraft/network/chat/Component;" +
                "Lnet/minecraft/network/chat/MessageSignature;" +
                "Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;" +
                "Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
        ],
        at = At("HEAD"),
        argsOnly = true,
        ordinal = 0,
    )
    protected fun skysoftHighlightSphinxAnswer(contents: Component): Component =
        DianaSphinxAnswerHighlighter.highlight(contents)

    @Unique
    private fun skysoftNewMessageOffset(): Float {
        if (!ChatMotionSettings.isMessageMotionEnabled() || chatScrollbarPos != 0) {
            return 0.0f
        }
        val progress = skysoftMessageArrival.progress(ChatMotionSettings.newMessageDurationMillis())
        return ChatMotionProfile.messageDisplacement(getLineHeight(), progress)
    }
}
