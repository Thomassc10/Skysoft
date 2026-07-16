package com.skysoft.mixin

import com.skysoft.features.inventory.SlotBindingManager
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(GuiGraphicsExtractor::class)
abstract class GuiGraphicsExtractorMixin {
    @Inject(method = ["extractDeferredElements"], at = [At("HEAD")])
    protected fun skysoftQueueSlotBindingTooltipBeforeDeferredTooltips(
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        ci: CallbackInfo,
    ) {
        SlotBindingManager.renderTopLayer(this as GuiGraphicsExtractor)
    }
}
