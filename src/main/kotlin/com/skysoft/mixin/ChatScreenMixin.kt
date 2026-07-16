package com.skysoft.mixin

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.skysoft.features.chat.ChatMotionProfile
import com.skysoft.features.chat.ChatMotionSettings
import com.skysoft.utils.animation.AnimationClock
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ChatScreen
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ChatScreen::class)
abstract class ChatScreenMixin {
    @field:Unique
    private val skysoftOpeningMotion = AnimationClock()

    @field:Unique
    private var skysoftOpenDisplacement = 0.0f

    @Inject(method = ["init()V"], at = [At("TAIL")])
    protected fun skysoftBeginOpenAnimation(ci: CallbackInfo) {
        val player = Minecraft.getInstance().player
        if (ChatMotionSettings.isInputMotionEnabled() && player != null && !player.isSleeping) {
            skysoftOpeningMotion.restart()
        } else {
            skysoftOpeningMotion.stop()
        }
    }

    @WrapOperation(
        method = ["extractRenderState"],
        at = [
            At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
            ),
        ],
    )
    protected fun skysoftAnimateChatInputBackground(
        graphics: GuiGraphicsExtractor,
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int,
        color: Int,
        original: Operation<Void>,
    ) {
        skysoftOpenDisplacement = skysoftChatOpenDisplacement()
        if (skysoftOpenDisplacement == 0.0f) {
            original.call(graphics, minX, minY, maxX, maxY, color)
            return
        }

        graphics.pose().pushMatrix()
        try {
            graphics.pose().translate(0.0f, skysoftOpenDisplacement)
            original.call(graphics, minX, minY, maxX, maxY, color)
        } finally {
            graphics.pose().popMatrix()
        }
    }

    @WrapOperation(
        method = ["extractRenderState"],
        at = [
            At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/screens/Screen;" +
                    "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            ),
        ],
    )
    protected fun skysoftAnimateChatInput(
        screen: ChatScreen,
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        original: Operation<Void>,
    ) {
        if (skysoftOpenDisplacement == 0.0f) {
            original.call(screen, graphics, mouseX, mouseY, delta)
            return
        }

        graphics.pose().pushMatrix()
        try {
            graphics.pose().translate(0.0f, skysoftOpenDisplacement)
            original.call(screen, graphics, mouseX, mouseY, delta)
        } finally {
            graphics.pose().popMatrix()
        }
    }

    @Inject(method = ["removed"], at = [At("HEAD")])
    protected fun skysoftResetOpenAnimation(ci: CallbackInfo) {
        skysoftOpeningMotion.stop()
    }

    @Unique
    private fun skysoftChatOpenDisplacement(): Float {
        if (!ChatMotionSettings.isInputMotionEnabled()) {
            return 0.0f
        }

        val minecraft = Minecraft.getInstance()
        val progress = skysoftOpeningMotion.progress(ChatMotionSettings.chatInputDurationMillis())
        return ChatMotionProfile.inputDisplacement(minecraft.window.guiScaledHeight, progress)
    }
}
