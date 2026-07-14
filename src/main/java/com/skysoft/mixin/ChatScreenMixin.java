package com.skysoft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.skysoft.features.chat.ChatMotionProfile;
import com.skysoft.features.chat.ChatMotionSettings;
import com.skysoft.utils.animation.AnimationClock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Unique
    private final AnimationClock skysoft$openingMotion = new AnimationClock();

    @Unique
    private float skysoft$openDisplacement;

    @Inject(method = "init()V", at = @At("TAIL"))
    private void skysoft$beginOpenAnimation(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (ChatMotionSettings.isInputMotionEnabled() && player != null && !player.isSleeping()) {
            skysoft$openingMotion.restart();
        } else {
            skysoft$openingMotion.stop();
        }
    }

    @WrapOperation(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"
        )
    )
    private void skysoft$animateChatInputBackground(
        GuiGraphicsExtractor graphics,
        int minX,
        int minY,
        int maxX,
        int maxY,
        int color,
        Operation<Void> original
    ) {
        skysoft$openDisplacement = skysoft$chatOpenDisplacement();
        if (skysoft$openDisplacement == 0.0f) {
            original.call(graphics, minX, minY, maxX, maxY, color);
            return;
        }

        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(0.0f, skysoft$openDisplacement);
            original.call(graphics, minX, minY, maxX, maxY, color);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @WrapOperation(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
        )
    )
    private void skysoft$animateChatInput(
        ChatScreen screen,
        GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float delta,
        Operation<Void> original
    ) {
        if (skysoft$openDisplacement == 0.0f) {
            original.call(screen, graphics, mouseX, mouseY, delta);
            return;
        }

        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(0.0f, skysoft$openDisplacement);
            original.call(screen, graphics, mouseX, mouseY, delta);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void skysoft$resetOpenAnimation(CallbackInfo ci) {
        skysoft$openingMotion.stop();
    }

    @Unique
    private float skysoft$chatOpenDisplacement() {
        if (!ChatMotionSettings.isInputMotionEnabled()) {
            return 0.0f;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float progress = skysoft$openingMotion.progress(ChatMotionSettings.chatInputDurationMillis());
        return ChatMotionProfile.inputDisplacement(minecraft.getWindow().getGuiScaledHeight(), progress);
    }
}
