package com.skysoft.mixin;

import com.skysoft.features.inventory.SlotBindingManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
    @Inject(method = "extractDeferredElements", at = @At("TAIL"))
    private void skysoft$renderSlotBindingTooltipAboveDeferredTooltips(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        SlotBindingManager.renderTopLayer((GuiGraphicsExtractor) (Object) this);
    }
}
