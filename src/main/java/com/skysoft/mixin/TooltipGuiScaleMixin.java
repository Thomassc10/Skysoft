package com.skysoft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.skysoft.gui.scale.GuiScaleController;
import com.skysoft.gui.tooltip.TooltipViewport;
import com.skysoft.utils.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public class TooltipGuiScaleMixin {
    @WrapOperation(
        method = "lambda$setTooltipForNextFrameInternal$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V"
        )
    )
    private void skysoft$renderTooltipAtSeparateScale(
        GuiGraphicsExtractor graphics,
        Font font,
        List<ClientTooltipComponent> tooltip,
        int x,
        int y,
        ClientTooltipPositioner positioner,
        Identifier sprite,
        Operation<Void> original
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!GuiScaleController.scalesTooltip(MinecraftClient.screen(minecraft))) {
            ClientTooltipPositioner scrollingPositioner = TooltipViewport.decorate(font, tooltip, x, y, positioner);
            original.call(graphics, font, tooltip, x, y, scrollingPositioner, sprite);
            return;
        }

        Window window = minecraft.getWindow();
        GuiScaleController.ResolvedScales scales = GuiScaleController.resolve(MinecraftClient.screen(minecraft), window);
        int tooltipScale = scales.tooltip();
        if (window.getGuiScale() == tooltipScale) {
            ClientTooltipPositioner scrollingPositioner = TooltipViewport.decorate(font, tooltip, x, y, positioner);
            original.call(graphics, font, tooltip, x, y, scrollingPositioner, sprite);
            return;
        }

        int activeScale = Math.max(1, window.getGuiScale());
        int tooltipX = GuiScaleController.convertCoordinate(x, activeScale, tooltipScale);
        int tooltipY = GuiScaleController.convertCoordinate(y, activeScale, tooltipScale);
        float poseScale = tooltipScale / (float) activeScale;
        ClientTooltipPositioner scrollingPositioner = TooltipViewport.decorate(font, tooltip, tooltipX, tooltipY, positioner);
        graphics.pose().pushMatrix();
        try (GuiScaleController.WindowScaleOverride ignored =
                 GuiScaleController.useTooltipScale(MinecraftClient.screen(minecraft), window)) {
            graphics.pose().scale(poseScale, poseScale);
            original.call(graphics, font, tooltip, tooltipX, tooltipY, scrollingPositioner, sprite);
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
