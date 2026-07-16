package com.skysoft.mixin

import com.skysoft.features.inventory.SlotLockManager
import com.skysoft.utils.input.InputHandlingResult
import net.minecraft.client.player.LocalPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(LocalPlayer::class)
open class LocalPlayerSlotLockMixin {
    @Inject(method = ["drop"], at = [At("HEAD")], cancellable = true)
    protected fun skysoftProtectLockedSelectedSlot(all: Boolean, cir: CallbackInfoReturnable<Boolean>) {
        if (SlotLockManager.handleSelectedItemDrop(this as LocalPlayer) == InputHandlingResult.CONSUMED) {
            cir.setReturnValue(false)
        }
    }
}
