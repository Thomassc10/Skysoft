package com.skysoft.mixin

import net.minecraft.client.ToggleKeyMapping
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(ToggleKeyMapping::class)
interface ToggleKeyMappingAccessor {
    @Invoker("reset")
    fun skysoftReset()
}
