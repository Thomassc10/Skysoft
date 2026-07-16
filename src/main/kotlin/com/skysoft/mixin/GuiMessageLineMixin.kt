package com.skysoft.mixin

import com.skysoft.features.chat.ChatMotionSettings
import net.minecraft.client.multiplayer.chat.GuiMessage
import net.minecraft.client.multiplayer.chat.GuiMessageTag
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(GuiMessage.Line::class)
abstract class GuiMessageLineMixin {
    @Inject(method = ["tag"], at = [At("HEAD")], cancellable = true)
    protected fun skysoftHideMessageIndicator(cir: CallbackInfoReturnable<GuiMessageTag?>) {
        if (ChatMotionSettings.isMessageIndicatorHidden()) {
            cir.returnValue = null
        }
    }
}
