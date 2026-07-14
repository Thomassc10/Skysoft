package com.skysoft.features.event.diana

import com.skysoft.utils.WorldVec
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

internal data class DianaBurrowSurfaceCheck(
    val status: DianaBurrowSurfaceStatus,
    val block: String = "unloaded",
    val above: String = "unloaded",
    val secondAbove: String = "unloaded",
) {
    val isValid: Boolean get() = status == DianaBurrowSurfaceStatus.VALID
}

internal enum class DianaBurrowSurfaceStatus {
    UNLOADED,
    VALID,
    INVALID,
}

internal object DianaBurrowSurfaceValidator {
    fun check(location: WorldVec): DianaBurrowSurfaceCheck {
        val level = Minecraft.getInstance().level ?: return DianaBurrowSurfaceCheck(DianaBurrowSurfaceStatus.UNLOADED)
        val blockPos = BlockPos(location.x.toInt(), location.y.toInt(), location.z.toInt())
        return check(level, blockPos)
    }

    fun check(level: ClientLevel, blockPos: BlockPos): DianaBurrowSurfaceCheck {
        val above = blockPos.above()
        val secondAbove = blockPos.above(SECOND_BLOCK_ABOVE_OFFSET)
        if (!level.isLoaded(blockPos) || !level.isLoaded(above) || !level.isLoaded(secondAbove)) {
            return DianaBurrowSurfaceCheck(DianaBurrowSurfaceStatus.UNLOADED)
        }

        val block = level.getBlockState(blockPos)
        val aboveBlock = level.getBlockState(above)
        val secondAboveBlock = level.getBlockState(secondAbove)
        val status = if (isValidSurface(block, aboveBlock, secondAboveBlock)) {
            DianaBurrowSurfaceStatus.VALID
        } else {
            DianaBurrowSurfaceStatus.INVALID
        }
        return DianaBurrowSurfaceCheck(status, block.debugName(), aboveBlock.debugName(), secondAboveBlock.debugName())
    }

    fun isValid(level: ClientLevel, blockPos: BlockPos): Boolean {
        val above = blockPos.above()
        val secondAbove = blockPos.above(SECOND_BLOCK_ABOVE_OFFSET)
        if (!level.isLoaded(blockPos) || !level.isLoaded(above) || !level.isLoaded(secondAbove)) return false
        return isValidSurface(level.getBlockState(blockPos), level.getBlockState(above), level.getBlockState(secondAbove))
    }

    private fun isValidSurface(block: BlockState, above: BlockState, secondAbove: BlockState): Boolean =
        block.isGrassSurface() && above.isAir && secondAbove.isAir

    private fun BlockState.isGrassSurface(): Boolean =
        `is`(Blocks.GRASS_BLOCK) && fluidState.isEmpty

    private fun BlockState.debugName(): String =
        BuiltInRegistries.BLOCK.getKey(block).toString()

}

private const val SECOND_BLOCK_ABOVE_OFFSET = 2
