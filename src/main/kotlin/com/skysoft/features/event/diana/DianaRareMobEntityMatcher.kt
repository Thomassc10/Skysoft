package com.skysoft.features.event.diana

import com.skysoft.config.DianaRareMobOption
import com.skysoft.config.SkysoftConfigGui
import com.skysoft.data.hypixel.HypixelLocationState
import com.skysoft.features.misc.StaleSkyBlockMobPlayerModels
import com.skysoft.utils.EntityUtilities.cleanName
import com.skysoft.utils.WorldVec
import com.skysoft.utils.toWorldVec
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player

internal data class DianaTrackedMobSignal(
    val label: String,
    val location: WorldVec,
    val entity: LivingEntity?,
    val nameplate: ArmorStand?,
    val health: DianaMobHealth?,
)

internal data class DianaRareMobSignal(
    val mob: DianaRareMobOption,
    val location: WorldVec,
    val entity: LivingEntity?,
    val nameplate: ArmorStand?,
    val health: DianaMobHealth?,
)

internal object DianaRareMobEntityMatcher {
    fun visibleSignals(): List<DianaRareMobSignal> =
        visibleTrackedSignals(ALL_RARE_MOB_LABELS).mapNotNull { signal ->
            val rareMob = DianaRareMobOption.fromLabel(signal.label) ?: return@mapNotNull null
            DianaRareMobSignal(
                mob = rareMob,
                location = signal.location,
                entity = signal.entity,
                nameplate = signal.nameplate,
                health = signal.health,
            )
        }

    fun visibleTrackedSignals(labels: Collection<String>): List<DianaTrackedMobSignal> {
        if (labels.isEmpty()) return emptyList()
        val entities = allEntities()
        val nameplateSignals = entities.filterIsInstance<ArmorStand>()
            .mapNotNull { armorStand -> armorStand.signal(entities, labels) }
        val nameplateEntityIds = nameplateSignals.mapNotNullTo(mutableSetOf()) { it.entity?.id }
        val physicalSignals = entities.filterIsInstance<LivingEntity>()
            .filter { entity -> entity.id !in nameplateEntityIds }
            .mapNotNull { entity -> entity.physicalSignal(labels) }
        return nameplateSignals + physicalSignals
    }

    @JvmStatic
    fun shouldHideBuggedEntity(entity: Entity): Boolean =
        shouldHideStaleRarePlayerModel(entity) ||
            StaleSkyBlockMobPlayerModels.shouldHide(entity) ||
            shouldHideBuggedNameplate(entity)

    @JvmStatic
    fun shouldHideStaleRarePlayerModel(entity: Entity): Boolean {
        val config = SkysoftConfigGui.config()
        if (!shouldCheckStaleRarePlayerModels(config.fixes.hideGlitchMobs, HypixelLocationState.inSkyBlock)) {
            return false
        }
        val player = entity as? Player ?: return false
        if (player == Minecraft.getInstance().player || player.isRealPlayer()) return false
        val label = labelFromName(player.cleanName(), ALL_RARE_MOB_LABELS) ?: return false
        val labels = DianaRareMobOption.fromLabel(label)?.matchLabels ?: setOf(label)
        return !hasSupportingNameplate(player, labels, allEntities())
    }

    fun shouldCheckStaleRarePlayerModels(hideGlitchMobs: Boolean, inSkyBlock: Boolean): Boolean =
        hideGlitchMobs && inSkyBlock

    fun isBuggedNameplateText(name: String): Boolean =
        name in BUGGED_NAMEPLATES

    fun allEntities(): List<Entity> =
        Minecraft.getInstance().level?.entitiesForRendering()?.toList().orEmpty()

    private fun shouldHideBuggedNameplate(entity: Entity): Boolean {
        val config = SkysoftConfigGui.config()
        if (!config.fixes.hideBuggedNameplates) return false
        return entity is ArmorStand && isBuggedNameplateText(entity.cleanName())
    }

    private fun ArmorStand.signal(entities: List<Entity>, labels: Collection<String>): DianaTrackedMobSignal? {
        val name = cleanName()
        val label = labelFromName(name, labels) ?: return null
        val health = DianaMobTextParsers.parseHealth(name)
        if (health == null && !name.contains(label, ignoreCase = true)) return null
        val linkedEntity = linkedPhysicalEntity(entities)
        return DianaTrackedMobSignal(
            label = label,
            location = linkedEntity?.position()?.toWorldVec() ?: position().toWorldVec(),
            entity = linkedEntity,
            nameplate = this,
            health = health,
        )
    }

    private fun LivingEntity.physicalSignal(labels: Collection<String>): DianaTrackedMobSignal? {
        if (!isStandaloneRareSignalEntity()) return null
        val label = labelFromName(cleanName(), labels) ?: return null
        return DianaTrackedMobSignal(
            label = label,
            location = position().toWorldVec(),
            entity = this,
            nameplate = null,
            health = null,
        )
    }

    private fun ArmorStand.linkedPhysicalEntity(entities: List<Entity>): LivingEntity? {
        val nameplate = this
        fun LivingEntity.isTightNameplatePair(): Boolean {
            val dx = x - nameplate.x
            val dz = z - nameplate.z
            val verticalOffset = nameplate.y - y
            return dx * dx + dz * dz <= NAMEPLATE_PAIR_HORIZONTAL_DISTANCE_SQ &&
                verticalOffset >= 0.0 &&
                verticalOffset <= NAMEPLATE_PAIR_MAX_VERTICAL_DISTANCE
        }

        val byId = entities.firstOrNull { entity -> entity.id == id - 1 && entity is LivingEntity } as? LivingEntity
        if (byId != null && byId.isRarePhysicalEntity() && byId.isTightNameplatePair()) return byId
        return entities.filterIsInstance<LivingEntity>()
            .filter { entity -> entity.isRarePhysicalEntity() }
            .filter { entity -> entity.isTightNameplatePair() }
            .minByOrNull { entity -> entity.distanceToSqr(this) }
    }

    private fun LivingEntity.isStandaloneRareSignalEntity(): Boolean =
        isRarePhysicalEntity() && this !is Player

    private fun LivingEntity.isRarePhysicalEntity(): Boolean {
        val player = Minecraft.getInstance().player
        if (!isAlive) return false
        if (this == player) return false
        if (this is ArmorStand) return false
        if (this is Player && isRealPlayer()) return false
        return true
    }

    private fun hasSupportingNameplate(entity: LivingEntity, labels: Collection<String>, entities: List<Entity>): Boolean =
        entities.filterIsInstance<ArmorStand>()
            .any { armorStand ->
                labelFromName(armorStand.cleanName(), labels) != null &&
                    armorStand.linkedPhysicalEntity(entities)?.id == entity.id
            }

    private fun Player.isRealPlayer(): Boolean =
        uuid.version() == REAL_PLAYER_UUID_VERSION

    private fun labelFromName(name: String, labels: Collection<String>): String? =
        labels.firstOrNull { label -> name.contains(label, ignoreCase = true) }

    private const val NAMEPLATE_PAIR_HORIZONTAL_DISTANCE_SQ = 1.0
    private const val NAMEPLATE_PAIR_MAX_VERTICAL_DISTANCE = 4.0
    private const val REAL_PLAYER_UUID_VERSION = 4
    private val BUGGED_NAMEPLATES = setOf("☣ Bleeds: -")
    private val ALL_RARE_MOB_LABELS = DianaRareMobOption.entries.flatMap { it.matchLabels }
}
