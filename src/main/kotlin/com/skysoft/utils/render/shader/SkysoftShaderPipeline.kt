// SPDX-License-Identifier: LGPL-2.1-only
// Adapted from SkyHanni; see credits.md for attribution and source details.

package com.skysoft.utils.render.shader

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexFormat
import com.skysoft.SkysoftMod
import com.skysoft.utils.render.SkysoftDrawMode
import com.skysoft.utils.render.SkysoftPipelineBuilder
import net.minecraft.client.renderer.RenderPipelines

enum class SkysoftShaderPipeline(
    snippet: RenderPipeline.Snippet,
    vFormat: VertexFormat,
    drawMode: SkysoftDrawMode = SkysoftDrawMode.QUADS,
    blend: BlendFunction? = null,
    vertexShaderPath: String,
    fragmentShaderPath: String = vertexShaderPath,
    depthWrite: Boolean = true,
) {
    CIRCLE_DEFERRED(
        snippet = SkysoftPipelineBuilder.guiSnippet(),
        vFormat = SkysoftVertexFormats.POSITION_COLOR_ROUNDED,
        blend = BlendFunction.TRANSLUCENT,
        vertexShaderPath = "circle_deferred",
        depthWrite = false,
    ),
    ;

    private val pipeline: RenderPipeline = RenderPipelines.register(
        SkysoftPipelineBuilder.build(
            location = SkysoftMod.id(name.lowercase()),
            snippet = snippet,
            vertexFormat = vFormat,
            drawMode = drawMode,
            blend = blend,
            vertexShader = SkysoftMod.id(vertexShaderPath),
            fragmentShader = SkysoftMod.id(fragmentShaderPath),
            depthWrite = depthWrite,
        ),
    )

    operator fun invoke(): RenderPipeline = pipeline
}
