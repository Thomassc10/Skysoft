// SPDX-License-Identifier: LGPL-2.1-only
// Adapted from SkyHanni; see credits.md for attribution and source details.

package com.skysoft.utils.render.shader

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormatElement
import org.lwjgl.system.MemoryUtil

private typealias VFEType = VertexFormatElement.Type

object SkysoftVertexFormats {
    private val lastRegisteredId by lazy {
        (0 until VertexFormatElement.MAX_COUNT).filter { VertexFormatElement.byId(it) != null }.max()
    }

    enum class VertexElement(
        private val index: Int = 0,
        private val type: VFEType = VFEType.FLOAT,
        private val count: Int = 4,
    ) {
        ROUNDED_PARAMS_0,
        ROUNDED_PARAMS_1,
        ;

        private val registrationId: Int by lazy { lastRegisteredId + ordinal + 1 }
        val element: VertexFormatElement by lazy {
            safeRegister(registrationId, index, type, false, count)
        }
    }

    val POSITION_COLOR_ROUNDED: VertexFormat by lazy {
        VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("RoundedParams0", VertexElement.ROUNDED_PARAMS_0.element)
            .add("RoundedParams1", VertexElement.ROUNDED_PARAMS_1.element)
            .build()
    }

    private fun safeRegister(
        desiredId: Int,
        index: Int = 0,
        type: VFEType = VFEType.FLOAT,
        normalized: Boolean = false,
        count: Int = 4,
    ): VertexFormatElement {
        val id = (desiredId until VertexFormatElement.MAX_COUNT).first { VertexFormatElement.byId(it) == null }
        return VertexFormatElement.register(id, index, type, normalized, count)
    }

    fun BufferBuilder.writeParams(
        x: Float,
        y: Float,
        z: Float,
        w: Float,
        format: VertexElement,
    ) {
        val ptr = (this@writeParams as VertexMemoryAccess).`skysoft$attributeAddress`(format.element, 0)
        writeParams(ptr, x, y, z, w, format.name)
    }

    private fun writeParams(ptr: Long, x: Float, y: Float, z: Float, w: Float, name: String) {
        check(ptr != -1L) { "Vertex format is missing $name" }
        MemoryUtil.memPutFloat(ptr, x)
        MemoryUtil.memPutFloat(ptr + Y_COMPONENT_BYTE_OFFSET, y)
        MemoryUtil.memPutFloat(ptr + Z_COMPONENT_BYTE_OFFSET, z)
        MemoryUtil.memPutFloat(ptr + W_COMPONENT_BYTE_OFFSET, w)
    }

    private const val FLOAT_BYTE_SIZE = 4L
    private const val Y_COMPONENT_BYTE_OFFSET = FLOAT_BYTE_SIZE
    private const val Z_COMPONENT_BYTE_OFFSET = FLOAT_BYTE_SIZE * 2
    private const val W_COMPONENT_BYTE_OFFSET = FLOAT_BYTE_SIZE * 3
}
