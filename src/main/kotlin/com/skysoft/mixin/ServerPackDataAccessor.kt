package com.skysoft.mixin

import com.google.common.hash.HashCode
import java.util.UUID
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(targets = ["net.minecraft.client.resources.server.ServerPackManager\$ServerPackData"])
interface ServerPackDataAccessor {
    @Accessor("id")
    fun skysoftGetId(): UUID

    @Accessor("hash")
    fun skysoftGetHash(): HashCode
}
