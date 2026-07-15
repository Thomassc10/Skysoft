package com.skysoft.features.helditem

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.skysoft.config.HeldItemTransformConfig
import com.skysoft.config.SkysoftConfigGui
import com.skysoft.data.skyblock.SkyBlockItemId.skyBlockId
import kotlin.math.tan
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import org.joml.Matrix4f
import org.joml.Matrix4fc

object HeldItemTransforms {
    private var newestCustomData: Any? = null
    private var newestItemId: String? = null
    private var previousCustomData: Any? = null
    private var previousItemId: String? = null

    @JvmStatic
    fun apply(itemStack: ItemStack, poseStack: PoseStack) {
        val config = SkysoftConfigGui.config().gui.heldItem
        if (!config.enabled) return
        val transform = effectiveTransform(itemStack)
        if (!transform.hasRenderChanges()) return

        applyPosition(poseStack, transform, RenderSystem.getModelViewStack())
        if (transform.scale != 1f) poseStack.scale(transform.scale, transform.scale, transform.scale)
    }

    fun currentItem(): ItemStack {
        val player = Minecraft.getInstance().player ?: return ItemStack.EMPTY
        return player.mainHandItem.takeUnless(ItemStack::isEmpty) ?: player.offhandItem
    }

    fun itemId(itemStack: ItemStack): String? {
        val customData = itemStack.get(DataComponents.CUSTOM_DATA) ?: return null
        if (customData === newestCustomData) return newestItemId
        if (customData === previousCustomData) return previousItemId

        val itemId = itemStack.skyBlockId()
        previousCustomData = newestCustomData
        previousItemId = newestItemId
        newestCustomData = customData
        newestItemId = itemId
        return itemId
    }

    fun effectiveTransform(itemId: String?): HeldItemTransformConfig =
        SkysoftConfigGui.config().gui.heldItem.transformFor(itemId)

    fun effectiveTransform(itemStack: ItemStack): HeldItemTransformConfig {
        HeldItemEditorScreen.previewTransform(itemStack)?.let { return it }
        val config = SkysoftConfigGui.config().gui.heldItem
        return if (config.itemTransforms.isEmpty()) config.global else config.transformFor(itemId(itemStack))
    }

    internal fun applyPosition(
        poseStack: PoseStack,
        transform: HeldItemTransformConfig,
        viewTransform: Matrix4fc,
    ) {
        val referenceDepth = HeldItemPositionMath.referenceDepth(transform.z)
        val screenTransform = Matrix4f()
            .m20(-transform.x / referenceDepth)
            .m21(-transform.y / referenceDepth)
            .translate(0f, 0f, transform.z)
        val cameraSpaceTransform = Matrix4f(viewTransform)
            .invert()
            .mul(screenTransform)
            .mul(viewTransform)
        poseStack.last().pose().mulLocal(cameraSpaceTransform)
    }
}

internal object HeldItemPositionMath {
    fun referenceDepth(depthOffset: Float): Float =
        (DEFAULT_ITEM_DEPTH - depthOffset).coerceAtLeast(MIN_ITEM_DEPTH)

    fun unitsPerPixel(guiHeight: Int, depthOffset: Float): Float {
        require(guiHeight > 0)
        return (2.0 * referenceDepth(depthOffset) * tan(HALF_HUD_FOV_RADIANS) / guiHeight).toFloat()
    }

    private const val DEFAULT_ITEM_DEPTH = 0.72f
    private const val MIN_ITEM_DEPTH = 0.1f
    private val HALF_HUD_FOV_RADIANS = Math.toRadians(Camera.BASE_HUD_FOV.toDouble() / 2.0)
}
