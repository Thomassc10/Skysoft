package com.skysoft.mixin

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.VertexFormatElement
import com.skysoft.utils.render.shader.VertexMemoryAccess
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow

@Mixin(BufferBuilder::class)
abstract class BufferBuilderMixin : VertexMemoryAccess {
    @Shadow
    private var vertexPointer = 0L

    override fun skysoftAttributeAddress(element: VertexFormatElement, byteOffset: Int): Long =
        if (vertexPointer == -1L) -1L else vertexPointer + byteOffset
}
