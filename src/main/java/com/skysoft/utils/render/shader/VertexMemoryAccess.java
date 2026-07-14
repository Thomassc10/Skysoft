package com.skysoft.utils.render.shader;

import com.mojang.blaze3d.vertex.VertexFormatElement;

public interface VertexMemoryAccess {
    long skysoft$attributeAddress(VertexFormatElement element, int byteOffset);
}
