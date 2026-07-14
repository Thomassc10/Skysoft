package com.skysoft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skysoft.features.helditem.HeldItemTransforms;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void skysoft$transformHeldItem(
        LivingEntity entity,
        ItemStack itemStack,
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int light,
        CallbackInfo ci
    ) {
        if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
            || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            HeldItemTransforms.apply(itemStack, poseStack);
        }
    }

    @Inject(method = "renderMap", at = @At("HEAD"))
    private void skysoft$transformHeldMap(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int light,
        ItemStack itemStack,
        CallbackInfo ci
    ) {
        HeldItemTransforms.apply(itemStack, poseStack);
    }
}
