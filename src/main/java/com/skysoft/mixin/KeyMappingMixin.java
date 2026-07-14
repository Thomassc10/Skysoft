package com.skysoft.mixin;

import com.skysoft.config.SkysoftConfigGui;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void skysoft$autoSprint(CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null || (Object) this != minecraft.options.keySprint) {
            return;
        }
        if (!SkysoftConfigGui.INSTANCE.config().misc.autoSprint) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.isSprinting()) {
            return;
        }

        cir.setReturnValue(true);
    }
}
