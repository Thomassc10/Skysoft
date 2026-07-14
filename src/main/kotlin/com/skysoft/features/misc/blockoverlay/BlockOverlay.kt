package com.skysoft.features.misc.blockoverlay

import com.mojang.brigadier.Command
import com.skysoft.config.SkysoftConfigGui
import com.skysoft.data.hypixel.HypixelLocationState
import com.skysoft.data.skyblock.SkyBlockEventState
import com.skysoft.data.skyblock.SkyBlockItemId.skyBlockId
import com.skysoft.features.pets.CanonicalItemNames
import com.skysoft.utils.ColorUtilities.toColor
import com.skysoft.utils.SkysoftChat
import com.skysoft.utils.render.BlockHighlightRenderer
import com.skysoft.utils.render.SkysoftRenderContext
import com.skysoft.utils.render.WorldRenderDispatcher
import com.skysoft.utils.toWorldVec
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import java.awt.Color
import kotlin.math.roundToInt

object BlockOverlay {
    private var pendingTarget: BlockOverlayTarget? = null
    private val activationCache = BlockOverlayActivationCache()

    fun register() {
        BlockOverlayItemCatalogue.startSession(config.settings.combinations)
        WorldRenderDispatcher.registerHandler(::renderWorld)
    }

    @JvmStatic
    fun selectBlockOutline(vanillaEligible: Boolean): BlockOutlineSelection {
        pendingTarget = null
        val minecraft = Minecraft.getInstance()
        val rawTarget = minecraft.hitResult as? BlockHitResult
        val target = rawTarget?.takeIf {
            val blockState = minecraft.level?.getBlockState(it.blockPos)
            isRenderableBlockTarget(it.type == HitResult.Type.BLOCK, blockState == null || blockState.isAir)
        }
        val heldItemId = if (config.enabled && target != null) minecraft.player?.mainHandItem?.skyBlockId() else null
        val conditionsMatch = config.enabled && target != null && conditionsMatch(heldItemId)
        if (!vanillaEligible || !config.enabled || target == null || !conditionsMatch) {
            return if (vanillaEligible) BlockOutlineSelection.VANILLA else BlockOutlineSelection.NONE
        }

        val blockTarget = requireNotNull(target)
        val level = minecraft.level
        val cameraEntity = minecraft.cameraEntity
        val shape = if (level != null && cameraEntity != null) {
            level.getBlockState(blockTarget.blockPos)
                .getShape(level, blockTarget.blockPos, CollisionContext.of(cameraEntity))
        } else {
            null
        }
        if (
            !shouldReplaceVanillaBlockOutline(
                isVanillaEligible = vanillaEligible,
                isEnabled = config.enabled,
                hasBlockTarget = true,
                doConditionsMatch = conditionsMatch,
                hasRenderableShape = shape != null && !shape.isEmpty,
            )
        ) {
            return BlockOutlineSelection.VANILLA
        }

        pendingTarget = BlockOverlayTarget(blockTarget.blockPos, requireNotNull(shape))
        return BlockOutlineSelection.CUSTOM
    }

    fun addHeldItem(source: FabricClientCommandSource): Int {
        val stack = Minecraft.getInstance().player?.mainHandItem
        val rawItemId = stack?.skyBlockId()
        val resolution = BlockOverlayItemInput.resolve(
            isEmpty = stack == null || stack.isEmpty,
            itemId = rawItemId,
            canonicalName = rawItemId?.let(CanonicalItemNames::resolve),
        )
        if (resolution is BlockOverlayItemInputResult.Rejected) return rejectItem(source, resolution.reason)
        resolution as BlockOverlayItemInputResult.Ready
        val itemId = resolution.itemId
        val cleanName = resolution.cleanName

        val label = "Holding Item: $cleanName"
        if (
            BlockOverlayItemCatalogue.addActiveItem(config.settings.combinations, itemId, label) ==
            BlockOverlayItemAddResult.ALREADY_AVAILABLE
        ) {
            SkysoftChat.error(source, "That item is already available in Block Overlay combinations.")
            return 0
        }
        BlockOverlayRules.markChanged()
        SkysoftConfigGui.config().saveNow()
        SkysoftChat.feedback(source, "Added $cleanName as a new Block Overlay combination.")
        return Command.SINGLE_SUCCESS
    }

    private fun rejectItem(source: FabricClientCommandSource, rejection: BlockOverlayItemRejection): Int {
        val message = when (rejection) {
            BlockOverlayItemRejection.EMPTY_HAND -> "Hold a SkyBlock item first."
            BlockOverlayItemRejection.MISSING_ID -> "The held item has no SkyBlock item ID."
            BlockOverlayItemRejection.NAME_UNAVAILABLE ->
                "The clean item name is still loading. Try the command again shortly."
        }
        SkysoftChat.error(source, message)
        return 0
    }

    private fun conditionsMatch(heldItemId: String?): Boolean {
        val key = BlockOverlayActivationKey(
            locationVersion = HypixelLocationState.locationVersion,
            eventVersion = SkyBlockEventState.version,
            rulesVersion = BlockOverlayRules.version,
            heldItemId = heldItemId,
        )
        return activationCache.conditionsMatch(key) {
            BlockOverlayConditions.matches(
                config.settings.combinations,
                BlockOverlayConditionContext(
                    isInSkyBlock = HypixelLocationState.inSkyBlock,
                    island = HypixelLocationState.currentIsland,
                    activeEvents = SkyBlockEventState.activeEvents(),
                    heldItemId = heldItemId,
                ),
            )
        }
    }

    private fun renderWorld(context: SkysoftRenderContext) {
        val target = pendingTarget ?: return
        pendingTarget = null
        val color = config.settings.color.get().toColor()
        val fillColor = Color(
            color.red,
            color.green,
            color.blue,
            (color.alpha * FILL_ALPHA_SCALE).roundToInt(),
        )
        BlockHighlightRenderer.drawShape(
            context,
            target.position.toWorldVec(),
            target.shape,
            color,
            fillColor,
            lineWidth = LINE_WIDTH,
            depth = true,
        )
    }

    private val config
        get() = SkysoftConfigGui.config().misc.blockOverlay

    private const val FILL_ALPHA_SCALE = 0.2
    private const val LINE_WIDTH = 3
}

enum class BlockOutlineSelection(val rendersVanilla: Boolean) {
    NONE(false),
    VANILLA(true),
    CUSTOM(false),
}

internal fun shouldReplaceVanillaBlockOutline(
    isVanillaEligible: Boolean,
    isEnabled: Boolean,
    hasBlockTarget: Boolean,
    doConditionsMatch: Boolean,
    hasRenderableShape: Boolean,
): Boolean = isVanillaEligible && isEnabled && hasBlockTarget && doConditionsMatch && hasRenderableShape

internal fun isRenderableBlockTarget(isBlockHit: Boolean, isAir: Boolean): Boolean = isBlockHit && !isAir

private data class BlockOverlayTarget(
    val position: BlockPos,
    val shape: VoxelShape,
)

internal data class BlockOverlayActivationKey(
    val locationVersion: Long,
    val eventVersion: Long,
    val rulesVersion: Long,
    val heldItemId: String?,
)

internal class BlockOverlayActivationCache {
    private var cachedKey: BlockOverlayActivationKey? = null
    private var cachedValue = false

    fun conditionsMatch(key: BlockOverlayActivationKey, calculateConditionsMatch: () -> Boolean): Boolean {
        if (key == cachedKey) return cachedValue
        cachedKey = key
        cachedValue = calculateConditionsMatch()
        return cachedValue
    }
}
