package com.skysoft.utils.render

import com.skysoft.utils.MinecraftRenderer
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.state.level.CameraRenderState

object WorldRenderDispatcher {
    private var handlers: List<(SkysoftRenderContext) -> Unit> = emptyList()

    fun register() {
        LevelRenderEvents.COLLECT_SUBMITS.register { context ->
            val minecraft = Minecraft.getInstance()
            val partialTicks = partialTicks()
            val camera = MinecraftRenderer.mainCamera(minecraft.gameRenderer)
            val cameraRenderState = CameraRenderState()
            camera.extractRenderState(cameraRenderState, partialTicks)
            val skysoftContext = SkysoftRenderContext(
                context.poseStack(),
                context.submitNodeCollector(),
                partialTicks,
                camera,
                cameraRenderState,
            )
            handlers.forEach { handler -> handler(skysoftContext) }
        }
    }

    fun registerHandler(handler: (SkysoftRenderContext) -> Unit) {
        handlers += handler
    }

    private fun partialTicks(): Float =
        Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(false)
}
