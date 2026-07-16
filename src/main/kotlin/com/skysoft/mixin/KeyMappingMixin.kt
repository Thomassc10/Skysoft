package com.skysoft.mixin

import com.skysoft.config.SkysoftConfigGui
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(KeyMapping::class)
open class KeyMappingMixin {
    @Inject(method = ["isDown"], at = [At("HEAD")], cancellable = true)
    protected fun skysoftAutoSprint(cir: CallbackInfoReturnable<Boolean>) {
        val minecraft = Minecraft.getInstance()
        if (minecraft == null || minecraft.options == null || (this as Any) !== minecraft.options.keySprint) return
        if (!SkysoftConfigGui.config().misc.autoSprint) return

        val player: LocalPlayer = minecraft.player ?: return
        if (player.isSprinting) return

        cir.setReturnValue(true)
    }
}
