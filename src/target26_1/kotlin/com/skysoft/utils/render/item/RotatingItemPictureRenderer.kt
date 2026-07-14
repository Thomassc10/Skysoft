// SPDX-License-Identifier: LGPL-2.1-only
// Adapted from SkyHanni; see credits.md for attribution and source details.

package com.skysoft.utils.render.item

import com.mojang.blaze3d.vertex.PoseStack
import com.skysoft.utils.MinecraftRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.state.gui.GuiRenderState

class RotatingItemPictureRenderer(
    bufferSource: MultiBufferSource.BufferSource,
) : PictureInPictureRenderer<RotatingItemPicture>(bufferSource) {
    override fun renderToTexture(state: RotatingItemPicture, poseStack: PoseStack) {
        poseStack.scale(1f, -1f, -1f)
        val dispatcher = MinecraftRenderer.featureRenderDispatcher(Minecraft.getInstance().gameRenderer)
        state.drawModel(poseStack, dispatcher.submitNodeStorage)
        dispatcher.renderAllFeatures()
    }

    override fun blitTexture(state: RotatingItemPicture, guiRenderState: GuiRenderState) {
        ItemPictureBlitter.queue(this, state, guiRenderState)
    }

    override fun getTranslateY(textureHeight: Int, guiScale: Int): Float = textureHeight.toFloat() / 2

    override fun getRenderStateClass(): Class<RotatingItemPicture> = RotatingItemPicture::class.java

    override fun getTextureLabel(): String = "skysoft_item"
}
