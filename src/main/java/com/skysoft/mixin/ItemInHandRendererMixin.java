package com.skysoft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skysoft.features.helditem.HeldItemSwingVisuals;
import com.skysoft.features.helditem.HeldItemTransforms;
import com.skysoft.features.helditem.SwingReplacementResult;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
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
            HeldItemSwingVisuals.apply(itemStack, poseStack);
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void skysoft$beginHeldItemSwing(
        AbstractClientPlayer player,
        float frameInterp,
        float xRot,
        InteractionHand hand,
        float attack,
        ItemStack itemStack,
        float inverseArmHeight,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int light,
        CallbackInfo ci
    ) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        HeldItemSwingVisuals.begin(itemStack, attack, arm);
    }

    @Inject(method = "renderArmWithItem", at = @At("TAIL"))
    private void skysoft$endHeldItemSwing(
        AbstractClientPlayer player,
        float frameInterp,
        float xRot,
        InteractionHand hand,
        float attack,
        ItemStack itemStack,
        float inverseArmHeight,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int light,
        CallbackInfo ci
    ) {
        HeldItemSwingVisuals.end();
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void skysoft$replaceHeldItemSwing(
        float attack,
        PoseStack poseStack,
        int invert,
        HumanoidArm arm,
        CallbackInfo ci
    ) {
        if (HeldItemSwingVisuals.replaceVanillaSwing() == SwingReplacementResult.REPLACED) {
            ci.cancel();
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
