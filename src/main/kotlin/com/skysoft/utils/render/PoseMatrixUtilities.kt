package com.skysoft.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf

object PoseMatrixUtilities {
    fun PoseStack.applyRotation(rotationVector: Vec3): PoseRotationResult {
        val xRad = Math.toRadians(rotationVector.x % FULL_ROTATION_DEGREES).toFloat()
        val yRad = Math.toRadians(rotationVector.y % FULL_ROTATION_DEGREES).toFloat()
        val zRad = Math.toRadians(rotationVector.z % FULL_ROTATION_DEGREES).toFloat()
        if (xRad == 0f && yRad == 0f && zRad == 0f) return PoseRotationResult.SKIPPED

        mulPose(Quaternionf().rotateXYZ(xRad, yRad, zRad))
        return PoseRotationResult.APPLIED
    }

    enum class PoseRotationResult {
        APPLIED,
        SKIPPED,
    }

    private const val FULL_ROTATION_DEGREES = 360
}
