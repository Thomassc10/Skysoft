// SPDX-License-Identifier: LGPL-2.1-only
// Adapted from SkyHanni; see credits.md for attribution and source details.

package com.skysoft.utils.render.shader

data class RoundedRectShaderParams(
    val cornerRadius: Float,
    val shaderHalfWidth: Float,
    val shaderHalfHeight: Float,
    val shaderCenterX: Float,
    val shaderCenterY: Float,
    val poseScaleX: Float,
    val poseScaleY: Float,
    val poseOffsetX: Float,
    val poseOffsetY: Float,
) {
    fun poseX(rawX: Float): Float = poseScaleX * rawX + poseOffsetX

    fun poseY(rawY: Float): Float = poseScaleY * rawY + poseOffsetY
}
