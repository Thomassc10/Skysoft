package com.skysoft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.skysoft.features.chat.ChatMotionProfile;
import com.skysoft.features.chat.ChatMotionSettings;
import com.skysoft.features.event.diana.DianaSphinxAnswerHighlighter;
import com.skysoft.utils.animation.AnimationClock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Shadow
    private int chatScrollbarPos;

    @Unique
    private final AnimationClock skysoft$messageArrival = new AnimationClock();

    @Shadow
    private int getLineHeight() {
        return 0;
    }

    @WrapOperation(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"
        )
    )
    private void skysoft$animateNewMessages(
        ChatComponent chat,
        ChatComponent.ChatGraphicsAccess graphicsAccess,
        int screenHeight,
        int ticks,
        ChatComponent.DisplayMode displayMode,
        Operation<Void> original,
        @Local(argsOnly = true) GuiGraphicsExtractor graphics
    ) {
        float displacement = skysoft$newMessageOffset();
        if (displacement == 0.0f) {
            original.call(chat, graphicsAccess, screenHeight, ticks, displayMode);
            return;
        }

        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(0.0f, displacement);
            original.call(chat, graphicsAccess, screenHeight, ticks, displayMode);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
        at = @At("TAIL")
    )
    private void skysoft$recordMessageTime(
        Component contents,
        MessageSignature signature,
        GuiMessageSource source,
        GuiMessageTag tag,
        CallbackInfo ci
    ) {
        skysoft$messageArrival.restart();
    }

    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private Component skysoft$highlightSphinxAnswer(Component contents) {
        return DianaSphinxAnswerHighlighter.highlight(contents);
    }

    @Unique
    private float skysoft$newMessageOffset() {
        if (!ChatMotionSettings.isMessageMotionEnabled() || chatScrollbarPos != 0) {
            return 0.0f;
        }
        float progress = skysoft$messageArrival.progress(ChatMotionSettings.newMessageDurationMillis());
        return ChatMotionProfile.messageDisplacement(getLineHeight(), progress);
    }
}
