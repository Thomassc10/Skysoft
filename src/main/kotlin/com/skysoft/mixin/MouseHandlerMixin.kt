package com.skysoft.mixin

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.mojang.blaze3d.platform.Window
import com.skysoft.features.bazaar.BazaarTracker
import com.skysoft.features.inventory.StorageOverlayController
import com.skysoft.gui.scale.CursorController
import com.skysoft.gui.scale.GuiScaleController
import com.skysoft.gui.scale.InventoryCursorMemory
import com.skysoft.gui.tooltip.TooltipViewport
import com.skysoft.utils.MinecraftClient
import com.skysoft.utils.input.InputHandlingResult
import net.minecraft.client.MouseHandler
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonInfo
import org.objectweb.asm.Opcodes
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(MouseHandler::class)
open class MouseHandlerMixin : CursorController {
    @field:Shadow
    private var xpos = 0.0

    @field:Shadow
    private var ypos = 0.0

    override fun skysoftMoveCursor(cursorX: Double, cursorY: Double) {
        xpos = cursorX
        ypos = cursorY
    }

    @Inject(
        method = ["grabMouse"],
        at = [
            At(
                value = "FIELD",
                target = "Lnet/minecraft/client/MouseHandler;mouseGrabbed:Z",
                opcode = Opcodes.PUTFIELD,
            ),
        ],
    )
    protected fun skysoftSaveCursorBeforeGrab(ci: CallbackInfo) {
        InventoryCursorMemory.beginMouseGrab(xpos, ypos)
    }

    @Inject(
        method = ["grabMouse"],
        at = [
            At(
                value = "INVOKE",
                target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(" +
                    "Lcom/mojang/blaze3d/platform/Window;IDD)V",
            ),
        ],
    )
    protected fun skysoftSaveCursorAfterGrab(ci: CallbackInfo) {
        InventoryCursorMemory.finishMouseGrab(xpos, ypos)
    }

    @Inject(method = ["onButton"], at = [At("HEAD")], cancellable = true)
    protected fun skysoftClickBazaarTrackerControl(
        window: Long,
        buttonInfo: MouseButtonInfo,
        action: Int,
        ci: CallbackInfo,
    ) {
        if (
            action == GLFW.GLFW_PRESS &&
            BazaarTracker.handleMouseButtonPress(buttonInfo.button()) == InputHandlingResult.CONSUMED
        ) {
            ci.cancel()
        }
    }

    @WrapOperation(
        method = ["onScroll"],
        at = [
            At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z",
            ),
        ],
    )
    protected fun doesSkysoftHandleTooltipScroll(
        screen: Screen,
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double,
        original: Operation<Boolean>,
    ): Boolean {
        val container = screen as? AbstractContainerScreen<*>
        val overStorageScrollPanel = container != null &&
            StorageOverlayController.shouldPreferMouseScroll(container, mouseX, mouseY, verticalAmount)
        if (overStorageScrollPanel) {
            if (
                TooltipViewport.isStorageOverlayScrollKeyDown() &&
                TooltipViewport.didHandleStorageMouseScroll(horizontalAmount, verticalAmount)
            ) {
                return true
            }
            return original.call(screen, mouseX, mouseY, horizontalAmount, verticalAmount)
        }
        if (TooltipViewport.didHandleMouseScroll(horizontalAmount, verticalAmount)) return true
        return original.call(screen, mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    @WrapOperation(
        method = ["releaseMouse"],
        at = [
            At(
                value = "INVOKE",
                target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(" +
                    "Lcom/mojang/blaze3d/platform/Window;IDD)V",
            ),
        ],
    )
    protected fun skysoftRestoreCursorOnRelease(
        window: Window,
        cursorMode: Int,
        initialCursorX: Double,
        initialCursorY: Double,
        original: Operation<Void>,
    ) {
        var cursorX = initialCursorX
        var cursorY = initialCursorY
        InventoryCursorMemory.cursorForRelease(cursorX, cursorY)?.let { restored ->
            cursorX = restored.x
            cursorY = restored.y
            xpos = cursorX
            ypos = cursorY
        }
        original.call(window, cursorMode, cursorX, cursorY)
    }

    private companion object {
        @JvmStatic
        @Inject(
            method = ["getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D"],
            at = [At("HEAD")],
            cancellable = true,
        )
        private fun skysoftGetInventoryScaledX(
            window: Window,
            xPosition: Double,
            cir: CallbackInfoReturnable<Double>,
        ) {
            val screen = MinecraftClient.screen()
            if (
                !GuiScaleController.usesSeparateInventoryScale(screen) ||
                GuiScaleController.areOverlaysUsingNormalCoordinates()
            ) {
                return
            }

            GuiScaleController.useInventoryScale(screen, window).use {
                cir.setReturnValue(xPosition * window.guiScaledWidth / window.screenWidth.toDouble())
            }
        }

        @JvmStatic
        @Inject(
            method = ["getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D"],
            at = [At("HEAD")],
            cancellable = true,
        )
        private fun skysoftGetInventoryScaledY(
            window: Window,
            yPosition: Double,
            cir: CallbackInfoReturnable<Double>,
        ) {
            val screen = MinecraftClient.screen()
            if (
                !GuiScaleController.usesSeparateInventoryScale(screen) ||
                GuiScaleController.areOverlaysUsingNormalCoordinates()
            ) {
                return
            }

            GuiScaleController.useInventoryScale(screen, window).use {
                cir.setReturnValue(yPosition * window.guiScaledHeight / window.screenHeight.toDouble())
            }
        }
    }
}
