// SPDX-License-Identifier: LGPL-2.1-only
// Adapted from SkyHanni; see credits.md for attribution and source details.

package com.skysoft.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("ITEM_SNIPPET")
    static RenderPipeline.Snippet skysoft$getItemSnippet() {
        throw new AssertionError();
    }
}
