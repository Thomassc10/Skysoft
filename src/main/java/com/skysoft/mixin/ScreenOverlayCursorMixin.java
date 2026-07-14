package com.skysoft.mixin;

import com.skysoft.gui.scale.CursorController;
import com.skysoft.gui.scale.InventoryCursorMemory;
import com.skysoft.gui.scale.ScaledScreenState;
import com.skysoft.gui.tooltip.TooltipViewport;
import com.skysoft.features.inventory.StorageOverlayController;
import com.skysoft.gui.GuiOverlayLayer;
import com.skysoft.gui.GuiOverlayRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenOverlayCursorMixin implements ScaledScreenState {
    @Unique
    private int skysoft$inventoryGuiWidth = -1;

    @Unique
    private int skysoft$inventoryGuiHeight = -1;

    @Override
    public boolean skysoft$hasScaleDimensions() {
        return skysoft$inventoryGuiWidth >= 0 && skysoft$inventoryGuiHeight >= 0;
    }

    @Override
    public boolean skysoft$matchesScaleDimensions(int width, int height) {
        return skysoft$inventoryGuiWidth == width && skysoft$inventoryGuiHeight == height;
    }

    @Override
    public void skysoft$rememberScaleDimensions(int width, int height) {
        skysoft$inventoryGuiWidth = width;
        skysoft$inventoryGuiHeight = height;
    }

    @Override
    public void skysoft$forgetScaleDimensions() {
        skysoft$inventoryGuiWidth = -1;
        skysoft$inventoryGuiHeight = -1;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void skysoft$suppressStorageOverlayWidgets(
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        if (skysoft$isStorageOverlayActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void skysoft$saveCursorBeforeRemove(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        TooltipViewport.clear();
        InventoryCursorMemory.rememberScreenCursor(
            (Screen) (Object) this,
            minecraft.mouseHandler.xpos(),
            minecraft.mouseHandler.ypos()
        );
    }

    @Inject(
        method = "extractRenderStateWithTooltipAndSubtitles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            shift = At.Shift.AFTER
        )
    )
    private void skysoft$renderBelowScreenOverlays(
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        GuiOverlayRegistry.renderLayer(GuiOverlayLayer.BELOW_SCREEN, context);
        skysoft$renderStorageOverlay(context, mouseX, mouseY);
    }

    @Inject(method = "init(II)V", at = @At("HEAD"))
    private void skysoft$clearInventoryGuiSizeBeforeInit(int width, int height, CallbackInfo ci) {
        skysoft$forgetScaleDimensions();
    }

    @Inject(method = "init(II)V", at = @At("TAIL"))
    private void skysoft$restoreCursorAfterInit(int width, int height, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        InventoryCursorMemory.restoreWhenScreenInitializes(
            (Screen) (Object) this,
            minecraft.getWindow(),
            (CursorController) minecraft.mouseHandler
        );
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void skysoft$restoreCursorAfterDelayedRecenter(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        InventoryCursorMemory.continueRestore(
            (Screen) (Object) this,
            minecraft.getWindow(),
            (CursorController) minecraft.mouseHandler
        );
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void skysoft$clearInventoryGuiSizeBeforeResize(CallbackInfo ci) {
        skysoft$forgetScaleDimensions();
    }

    @Unique
    private boolean skysoft$isStorageOverlayActive() {
        return (Object) this instanceof AbstractContainerScreen<?> screen && StorageOverlayController.isActive(screen);
    }

    @Unique
    private void skysoft$renderStorageOverlay(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if ((Object) this instanceof ContainerScreen screen && StorageOverlayController.isActive(screen)) {
            context.nextStratum();
            StorageOverlayController.renderBackground(screen, context, mouseX, mouseY);
        }
    }
}
