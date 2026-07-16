package com.skysoft.utils.render.shader

import com.mojang.blaze3d.vertex.VertexFormatElement

interface VertexMemoryAccess {
    fun skysoftAttributeAddress(element: VertexFormatElement, byteOffset: Int): Long
}
