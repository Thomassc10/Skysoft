package com.skysoft.mixin;

import com.mojang.blaze3d.platform.Window;
import com.skysoft.gui.scale.GuiScaleController;
import com.skysoft.features.misc.blockoverlay.BlockOverlay;
import com.skysoft.gui.GuiOverlayLayer;
import com.skysoft.gui.GuiOverlayRegistry;
import com.skysoft.utils.MinecraftClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "shouldRenderBlockOutline", at = @At("RETURN"), cancellable = true)
    private void skysoft$replaceBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(BlockOverlay.selectBlockOutline(cir.getReturnValue()).getRendersVanilla());
    }

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private GuiRenderer guiRenderer;

    @Shadow
    @Final
    private GameRenderState gameRenderState;

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V",
            shift = At.Shift.AFTER
        )
    )
    private void skysoft$renderInventoryAtSeparateScale(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        GuiRenderState defaultRenderState = skysoft$getDefaultRenderState();
        Window window = minecraft.getWindow();
        GuiScaleController.RenderBatch renderStates = GuiScaleController.takeRenderBatch();
        if (renderStates != null) {
            try (GuiScaleController.WindowScaleOverride ignored =
                     GuiScaleController.useInventoryScale(MinecraftClient.screen(minecraft), window)) {
                skysoft$syncWindowScale(window);
                ((GuiRendererAccessor) guiRenderer).skysoft$setRenderState(renderStates.inventory());
                guiRenderer.render();
            } finally {
                ((GuiRendererAccessor) guiRenderer).skysoft$setRenderState(defaultRenderState);
                skysoft$syncWindowScale(window);
            }
        }

        GuiRenderState aboveScreenRenderState = renderStates == null
            ? new GuiRenderState()
            : renderStates.overlays();
        skysoft$renderAboveScreenState(defaultRenderState, aboveScreenRenderState, window, renderStates != null);
    }

    @Unique
    private void skysoft$renderAboveScreenState(
        GuiRenderState defaultRenderState,
        GuiRenderState aboveScreenRenderState,
        Window window,
        boolean hasSeparatedInventory
    ) {
        boolean hasSkysoftOverlays = GuiOverlayRegistry.shouldRenderLayer(GuiOverlayLayer.ABOVE_SCREEN);
        if (!hasSeparatedInventory && !hasSkysoftOverlays) {
            return;
        }

        if (hasSkysoftOverlays) {
            int mouseX = (int) minecraft.mouseHandler.getScaledXPos(window);
            int mouseY = (int) minecraft.mouseHandler.getScaledYPos(window);
            GuiGraphicsExtractor overlayGraphics = new GuiGraphicsExtractor(
                minecraft,
                aboveScreenRenderState,
                mouseX,
                mouseY
            );
            GuiOverlayRegistry.renderLayer(GuiOverlayLayer.ABOVE_SCREEN, overlayGraphics);
        }

        try {
            ((GuiRendererAccessor) guiRenderer).skysoft$setRenderState(aboveScreenRenderState);
            guiRenderer.render();
        } finally {
            ((GuiRendererAccessor) guiRenderer).skysoft$setRenderState(defaultRenderState);
        }
    }

    @Unique
    private GuiRenderState skysoft$getDefaultRenderState() {
        return gameRenderState.guiRenderState;
    }

    @Unique
    private void skysoft$syncWindowScale(Window window) {
        gameRenderState.windowRenderState.guiScale = window.getGuiScale();
    }
}
