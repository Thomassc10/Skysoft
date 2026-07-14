package com.skysoft.mixin

import net.minecraft.client.gui.Hud
import net.minecraft.network.chat.Component
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Hud::class)
interface OverlayMessageAccessor {
    @Accessor("overlayMessageString")
    fun skysoftGetOverlayMessageString(): Component?

    @Accessor("overlayMessageTime")
    fun skysoftGetOverlayMessageTime(): Int
}
