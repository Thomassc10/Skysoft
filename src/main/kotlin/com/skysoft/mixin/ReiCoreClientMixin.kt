package com.skysoft.mixin

import com.skysoft.features.inventory.StorageOverlayController
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Pseudo
@Mixin(targets = ["me.shedaniel.rei.RoughlyEnoughItemsCoreClient"], remap = false)
abstract class ReiCoreClientMixin private constructor() {
    private companion object {
        @JvmStatic
        @Inject(
            method = ["shouldReturn(Lnet/minecraft/client/gui/screens/Screen;)Z"],
            at = [At("HEAD")],
            cancellable = true,
            remap = false,
        )
        private fun skysoftShouldReturnForStorage(screen: Screen, cir: CallbackInfoReturnable<Boolean>) {
            val containerScreen = screen as? AbstractContainerScreen<*> ?: return
            if (StorageOverlayController.isActive(containerScreen)) cir.setReturnValue(true)
        }
    }
}
