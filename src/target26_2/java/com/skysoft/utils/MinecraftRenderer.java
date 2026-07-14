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
        return renderer.mainCamera();
    }

    public static Lighting lighting(GameRenderer renderer) {
        return renderer.lighting();
    }

    public static WindowRenderState windowRenderState(GameRenderer renderer) {
        return renderer.gameRenderState().windowRenderState;
    }

    public static FeatureRenderDispatcher featureRenderDispatcher(GameRenderer renderer) {
        return renderer.featureRenderDispatcher();
    }
}
