package com.skysoft.features.misc

import com.skysoft.config.SkysoftConfigGui
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand

object DeadEntityHider {
    @JvmStatic
    fun shouldHide(entity: Entity): Boolean {
        if (!SkysoftConfigGui.config().misc.hideDeadEntities) return false
        val dyingEntity = when {
            entity is LivingEntity && entity.isDeadOrDying -> entity
            entity is ArmorStand -> entity.linkedDyingEntity()
            else -> null
        } ?: return false

        return true
    }

    private fun ArmorStand.linkedDyingEntity(): LivingEntity? {
        if (!isInvisible || !isCustomNameVisible || customName == null) return null
        val level = Minecraft.getInstance().level ?: return null
        return (1..MAX_NAMEPLATE_ENTITY_ID_OFFSET).firstNotNullOfOrNull { offset ->
            val candidate = level.getEntity(id - offset) as? LivingEntity ?: return@firstNotNullOfOrNull null
            candidate.takeIf { entity -> entity !is ArmorStand && entity.isDeadOrDying && isNameplateFor(entity) }
        }
    }

    private fun ArmorStand.isNameplateFor(entity: LivingEntity): Boolean {
        val dx = x - entity.x
        val dz = z - entity.z
        val verticalOffset = y - entity.y
        return dx * dx + dz * dz <= NAMEPLATE_HORIZONTAL_DISTANCE_SQUARED &&
            verticalOffset in 0.0..NAMEPLATE_MAX_VERTICAL_DISTANCE
    }

    private const val MAX_NAMEPLATE_ENTITY_ID_OFFSET = 4
    private const val NAMEPLATE_HORIZONTAL_DISTANCE_SQUARED = 1.0
    private const val NAMEPLATE_MAX_VERTICAL_DISTANCE = 4.0
}
