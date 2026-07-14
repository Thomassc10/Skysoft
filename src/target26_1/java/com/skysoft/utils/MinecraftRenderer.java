package com.skysoft.utils;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.WindowRenderState;

public final class MinecraftRenderer {
    private MinecraftRenderer() {
    }

    public static Camera mainCamera(GameRenderer renderer) {
        return renderer.getMainCamera();
    }

    public static Lighting lighting(GameRenderer renderer) {
        return renderer.getLighting();
    }

    public static WindowRenderState windowRenderState(GameRenderer renderer) {
        return renderer.getGameRenderState().windowRenderState;
    }

    public static FeatureRenderDispatcher featureRenderDispatcher(GameRenderer renderer) {
        return renderer.getFeatureRenderDispatcher();
    }
}
