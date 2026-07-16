package com.skysoft.mixin

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.llamalad7.mixinextras.sugar.Local
import com.skysoft.gui.scale.GuiScaleController
import com.skysoft.gui.tooltip.TooltipViewport
import com.skysoft.utils.MinecraftClient
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner
import net.minecraft.resources.Identifier
import org.joml.Vector2ic
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(GuiGraphicsExtractor::class)
open class TooltipGuiScaleMixin {
    @WrapOperation(
        method = ["lambda\$setTooltipForNextFrameInternal\$0"],
        at = [
            At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;tooltip(" +
                    "Lnet/minecraft/client/gui/Font;Ljava/util/List;II" +
                    "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;" +
                    "Lnet/minecraft/resources/Identifier;)V",
            ),
        ],
    )
    protected fun skysoftRenderTooltipAtSeparateScale(
        graphics: GuiGraphicsExtractor,
        font: Font,
        tooltip: List<ClientTooltipComponent>,
        x: Int,
        y: Int,
        positioner: ClientTooltipPositioner,
        sprite: Identifier?,
        original: Operation<Void>,
    ) {
        val minecraft = Minecraft.getInstance()
        val screen = MinecraftClient.screen(minecraft)
        if (!GuiScaleController.usesSeparateTooltipScale(screen)) {
            original.call(graphics, font, tooltip, x, y, positioner, sprite)
            return
        }

        val window = minecraft.window
        val scales = GuiScaleController.resolve(screen, window)
        val tooltipScale = scales.tooltip()
        if (window.guiScale == tooltipScale) {
            original.call(graphics, font, tooltip, x, y, positioner, sprite)
            return
        }

        val activeScale = window.guiScale.coerceAtLeast(1)
        val tooltipX = GuiScaleController.convertCoordinate(x, activeScale, tooltipScale)
        val tooltipY = GuiScaleController.convertCoordinate(y, activeScale, tooltipScale)
        val poseScale = tooltipScale / activeScale.toFloat()
        graphics.pose().pushMatrix()
        try {
            GuiScaleController.useTooltipScale(screen, window).use {
                graphics.pose().scale(poseScale, poseScale)
                original.call(graphics, font, tooltip, tooltipX, tooltipY, positioner, sprite)
            }
        } finally {
            graphics.pose().popMatrix()
        }
    }

    @WrapOperation(
        method = [
            "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II" +
                "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;" +
                "Lnet/minecraft/resources/Identifier;)V",
        ],
        at = [
            At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;" +
                    "positionTooltip(IIIIII)Lorg/joml/Vector2ic;",
            ),
        ],
    )
    protected fun skysoftPositionScrollableTooltip(
        positioner: ClientTooltipPositioner,
        screenWidth: Int,
        screenHeight: Int,
        x: Int,
        y: Int,
        tooltipWidth: Int,
        tooltipHeight: Int,
        original: Operation<Vector2ic>,
        @Local(argsOnly = true) font: Font,
        @Local(argsOnly = true) tooltip: List<ClientTooltipComponent>,
    ): Vector2ic {
        val scrollingPositioner = TooltipViewport.decorate(font, tooltip, x, y, positioner)
        return original.call(scrollingPositioner, screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight)
    }
}
