package com.skysoft.mixin;

import com.skysoft.features.inventory.SlotLockManager;
import com.skysoft.utils.input.InputHandlingResult;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerSlotLockMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void skysoft$protectLockedSelectedSlot(boolean all, CallbackInfoReturnable<Boolean> cir) {
        if (SlotLockManager.handleSelectedItemDrop((LocalPlayer) (Object) this) == InputHandlingResult.CONSUMED) {
            cir.setReturnValue(false);
        }
    }
}
