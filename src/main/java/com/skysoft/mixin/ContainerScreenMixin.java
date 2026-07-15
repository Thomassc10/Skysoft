package com.skysoft.mixin;

import com.skysoft.features.inventory.StorageOverlayController;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void skysoft$suppressStorageOverlayBackground(
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        if (
            StorageOverlayController.isActive((ContainerScreen) (Object) this)
                && !StorageOverlayController.shouldDimBackground()
        ) {
            ci.cancel();
        }
    }

    @Inject(
        method = "extractBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;"
                + "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void skysoft$suppressStorageOverlayContainerBackground(
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        if (
            StorageOverlayController.isActive((ContainerScreen) (Object) this)
                && StorageOverlayController.shouldDimBackground()
        ) {
            ci.cancel();
        }
    }
}
