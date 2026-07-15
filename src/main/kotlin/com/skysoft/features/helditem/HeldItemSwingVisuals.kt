package com.skysoft.features.helditem

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.skysoft.config.HeldItemSwingStyle
import com.skysoft.config.SkysoftConfigGui
import kotlin.math.sin
import kotlin.math.sqrt
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwingAnimationType

object HeldItemSwingVisuals {
    private var itemOnlySwing: ItemOnlySwing? = null

    @JvmStatic
    fun begin(itemStack: ItemStack, attack: Float, arm: HumanoidArm) {
        val config = SkysoftConfigGui.config().gui.heldItem
        val transform = HeldItemTransforms.effectiveTransform(itemStack)
        itemOnlySwing = ItemOnlySwing(itemStack, attack, arm).takeIf {
            config.enabled &&
                transform.swingStyle == HeldItemSwingStyle.ITEM_ONLY &&
                itemStack.swingAnimation.type() == SwingAnimationType.WHACK
        }
    }

    @JvmStatic
    fun replaceVanillaSwing(): SwingReplacementResult {
        val swing = itemOnlySwing ?: return SwingReplacementResult.UNCHANGED
        swing.isVanillaSwingReplaced = true
        return SwingReplacementResult.REPLACED
    }

    @JvmStatic
    fun apply(itemStack: ItemStack, poseStack: PoseStack) {
        val swing = itemOnlySwing?.takeIf {
            it.itemStack === itemStack && it.isVanillaSwingReplaced
        } ?: return
        applyItemOnlySwing(poseStack, swing.attack, swing.arm)
    }

    @JvmStatic
    fun end() {
        itemOnlySwing = null
    }

    internal fun applyItemOnlySwing(poseStack: PoseStack, attack: Float, arm: HumanoidArm) {
        if (attack <= 0f) return
        val direction = if (arm == HumanoidArm.RIGHT) 1f else -1f
        val arc = sin(sqrt(attack.toDouble()) * Math.PI).toFloat()
        val twist = sin(attack * attack * Math.PI).toFloat()
        poseStack.mulPose(Axis.YP.rotationDegrees(direction * twist * SWING_Y_DEGREES))
        poseStack.mulPose(Axis.ZP.rotationDegrees(direction * arc * SWING_Z_DEGREES))
        poseStack.mulPose(Axis.XP.rotationDegrees(arc * SWING_X_DEGREES))
    }

    private data class ItemOnlySwing(
        val itemStack: ItemStack,
        val attack: Float,
        val arm: HumanoidArm,
        var isVanillaSwingReplaced: Boolean = false,
    )

    private const val SWING_X_DEGREES = -80f
    private const val SWING_Y_DEGREES = -20f
    private const val SWING_Z_DEGREES = -20f
}

enum class SwingReplacementResult {
    REPLACED,
    UNCHANGED,
}
