// SPDX-License-Identifier: LGPL-2.1-only
// Adapted from SkyHanni; see credits.md for attribution and source details.

package com.skysoft.utils.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexFormat
import com.skysoft.mixin.RenderPipelinesAccessor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import java.util.Optional

object SkysoftPipelineBuilder {
    fun guiSnippet(): RenderPipeline.Snippet = RenderPipelines.GUI_SNIPPET
    fun itemSnippet(): RenderPipeline.Snippet = RenderPipelinesAccessor.`skysoft$getItemSnippet`()

    fun build(
        location: Identifier,
        snippet: RenderPipeline.Snippet,
        vertexFormat: VertexFormat,
        drawMode: SkysoftDrawMode,
        blend: BlendFunction? = null,
        vertexShader: Identifier? = null,
        fragmentShader: Identifier? = null,
        shaderDefines: Map<String, Float> = emptyMap(),
        depthStencilState: DepthStencilState? = null,
        depthWrite: Boolean = true,
    ): RenderPipeline = RenderPipeline.builder(snippet)
        .withLocation(location)
        .withVertexFormat(vertexFormat, drawMode.toVertexFormatMode())
        .apply {
            blend?.let { withColorTargetState(ColorTargetState(it)) }
            vertexShader?.let { withVertexShader(it) }
            fragmentShader?.let { withFragmentShader(it) }
            shaderDefines.forEach(::withShaderDefine)
            if (depthStencilState != null) {
                withDepthStencilState(depthStencilState)
            } else if (!depthWrite) {
                withDepthStencilState(Optional.empty())
            }
        }
        .build()

    private fun SkysoftDrawMode.toVertexFormatMode(): VertexFormat.Mode = when (this) {
        SkysoftDrawMode.LINES -> VertexFormat.Mode.LINES
        SkysoftDrawMode.QUADS -> VertexFormat.Mode.QUADS
    }
}
