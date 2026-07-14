package com.skysoft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.skysoft.features.bazaar.BazaarTracker;
import com.skysoft.gui.scale.CursorController;
import com.skysoft.gui.scale.GuiScaleController;
import com.skysoft.gui.scale.InventoryCursorMemory;
import com.skysoft.utils.MinecraftClient;
import com.skysoft.utils.input.InputHandlingResult;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.objectweb.asm.Opcodes;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin implements CursorController {
    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Override
    public void skysoft$moveCursor(double cursorX, double cursorY) {
        xpos = cursorX;
        ypos = cursorY;
    }

    @Inject(
        method = "grabMouse",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/MouseHandler;mouseGrabbed:Z",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void skysoft$saveCursorBeforeGrab(CallbackInfo ci) {
        InventoryCursorMemory.beginMouseGrab(xpos, ypos);
    }

    @Inject(
        method = "grabMouse",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"
        )
    )
    private void skysoft$saveCursorAfterGrab(CallbackInfo ci) {
        InventoryCursorMemory.finishMouseGrab(xpos, ypos);
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void skysoft$clickBazaarTrackerControl(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (
            action == GLFW.GLFW_PRESS
                && BazaarTracker.handleMouseButtonPress(buttonInfo.button()) == InputHandlingResult.CONSUMED
        ) {
            ci.cancel();
        }
    }

    @WrapOperation(
        method = "releaseMouse",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"
        )
    )
    private void skysoft$restoreCursorOnRelease(
        Window window,
        int cursorMode,
        double cursorX,
        double cursorY,
        Operation<Void> original
    ) {
        InventoryCursorMemory.CursorPoint restored = InventoryCursorMemory.cursorForRelease(cursorX, cursorY);
        if (restored != null) {
            cursorX = restored.x();
            cursorY = restored.y();
            xpos = cursorX;
            ypos = cursorY;
        }

        original.call(window, cursorMode, cursorX, cursorY);
    }

    @Inject(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("HEAD"), cancellable = true)
    private static void skysoft$getInventoryScaledX(Window window, double xPosition, CallbackInfoReturnable<Double> cir) {
        if (!GuiScaleController.scalesInventory(MinecraftClient.screen()) ||
            GuiScaleController.overlaysUseNormalCoordinates()) {
            return;
        }

        try (GuiScaleController.WindowScaleOverride ignored =
                 GuiScaleController.useInventoryScale(MinecraftClient.screen(), window)) {
            cir.setReturnValue(xPosition * window.getGuiScaledWidth() / (double) window.getScreenWidth());
        }
    }

    @Inject(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("HEAD"), cancellable = true)
    private static void skysoft$getInventoryScaledY(Window window, double yPosition, CallbackInfoReturnable<Double> cir) {
        if (!GuiScaleController.scalesInventory(MinecraftClient.screen()) ||
            GuiScaleController.overlaysUseNormalCoordinates()) {
            return;
        }

        try (GuiScaleController.WindowScaleOverride ignored =
                 GuiScaleController.useInventoryScale(MinecraftClient.screen(), window)) {
            cir.setReturnValue(yPosition * window.getGuiScaledHeight() / (double) window.getScreenHeight());
        }
    }
}
