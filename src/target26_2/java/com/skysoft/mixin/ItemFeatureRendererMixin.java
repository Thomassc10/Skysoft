package com.skysoft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.skysoft.utils.render.WorldItemBadgeRenderer;
import com.skysoft.utils.render.WorldItemRenderLayers;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {
    @Unique
    private static final ThreadLocal<Boolean> skysoft$throughWalls = ThreadLocal.withInitial(() -> false);

    @Inject(method = "prepareMainSubmit", at = @At("HEAD"))
    private void skysoft$beginItemRender(ItemFeatureRenderer.Submit submit, CallbackInfo callbackInfo) {
        skysoft$throughWalls.set(submit.outlineColor() == WorldItemBadgeRenderer.THROUGH_WALLS_MARKER);
    }

    @Inject(method = "prepareMainSubmit", at = @At("RETURN"))
    private void skysoft$endItemRender(ItemFeatureRenderer.Submit submit, CallbackInfo callbackInfo) {
        skysoft$throughWalls.remove();
    }

    @WrapOperation(
        method = "prepareMainSubmit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;itemRenderType()Lnet/minecraft/client/renderer/rendertype/RenderType;"
        )
    )
    private RenderType skysoft$useThroughWallsRenderType(
        BakedQuad.MaterialInfo materialInfo,
        Operation<RenderType> original
    ) {
        RenderType renderType = original.call(materialInfo);
        if (!skysoft$throughWalls.get()) {
            return renderType;
        }
        return WorldItemRenderLayers.throughWalls(materialInfo.sprite().atlasLocation(), renderType.hasBlending());
    }

    @WrapOperation(
        method = "prepareSubmit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer$Submit;outlineColor()I"
        )
    )
    private int skysoft$hideRenderModeMarker(
        ItemFeatureRenderer.Submit submit,
        Operation<Integer> original
    ) {
        int outlineColor = original.call(submit);
        return outlineColor == WorldItemBadgeRenderer.THROUGH_WALLS_MARKER ? 0 : outlineColor;
    }
}
