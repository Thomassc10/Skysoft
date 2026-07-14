package com.skysoft.data.skyblock

import com.google.gson.Gson
import com.skysoft.SkysoftMod
import com.skysoft.utils.ElapsedTimeMark
import com.skysoft.utils.net.PendingHttpRequests
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes

object MayorPerkApi {
    private const val ELECTION_URL = "https://api.hypixel.net/v2/resources/skyblock/election"
    private const val SHARING_IS_CARING = "Sharing is Caring"
    private const val PET_XP_BUFF = "Pet XP Buff"
    private const val MYTHOLOGICAL_RITUAL = "Mythological Ritual"
    private const val CHIVALROUS_CARNIVAL = "Chivalrous Carnival"
    private const val FISHING_FESTIVAL = "Fishing Festival"
    private const val MINING_FIESTA = "Mining Fiesta"
    private const val REFRESH_CHECK_INTERVAL_TICKS = 40

    private val gson = Gson()
    private val requests = PendingHttpRequests()
    private val loading = AtomicBoolean(false)
    private var lastUpdate = ElapsedTimeMark.farPast()
    private var ticks = 0

    @Volatile
    var sharingIsCaringActive: Boolean = false
        private set

    @Volatile
    var petXpBuffActive: Boolean = false
        private set

    @Volatile
    var mythologicalRitualActive: Boolean = false
        private set

    @Volatile
    var carnivalActive: Boolean = false
        private set

    @Volatile
    var fishingFestivalActive: Boolean = false
        private set

    @Volatile
    var miningFiestaActive: Boolean = false
        private set

    @Volatile
    var mythologicalRitualEventKey: String? = null
        private set

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register tick@{
            if (++ticks % REFRESH_CHECK_INTERVAL_TICKS != 0) return@tick
            if (lastUpdate.passedSince() < 20.minutes) return@tick
            refresh()
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            requests.cancelAll()
        }
    }

    private fun refresh() {
        if (!loading.compareAndSet(false, true)) return
        lastUpdate = ElapsedTimeMark.now()
        requests.getString(ELECTION_URL)
            .thenApply { response -> gson.fromJson(response, ElectionResponse::class.java) }
            .whenComplete { response, error ->
                if (error == null && response != null) {
                    sharingIsCaringActive = response.hasPerk(SHARING_IS_CARING)
                    petXpBuffActive = response.hasPerk(PET_XP_BUFF)
                    mythologicalRitualActive = response.hasPerk(MYTHOLOGICAL_RITUAL)
                    carnivalActive = response.hasPerk(CHIVALROUS_CARNIVAL)
                    fishingFestivalActive = response.hasPerk(FISHING_FESTIVAL)
                    miningFiestaActive = response.hasPerk(MINING_FIESTA)
                    mythologicalRitualEventKey = response.mythologicalRitualEventKey()
                } else {
                    SkysoftMod.LOGGER.warn("Failed to refresh mayor perks", error)
                }
                loading.set(false)
            }
    }

    private fun ElectionResponse.hasPerk(perkName: String): Boolean =
        mayor.hasPerk(perkName) || mayor?.minister?.perk?.name == perkName

    private fun MayorEntry?.hasPerk(perkName: String): Boolean =
        this?.perks?.any { it.name == perkName } == true

    private fun ElectionResponse.mythologicalRitualEventKey(): String? {
        val mayorEntry = mayor ?: return null
        val electedYear = mayorEntry.election?.year?.takeIf { year -> year > 0 } ?: return null
        val minister = mayorEntry.minister
        return when {
            mayorEntry.hasPerk(MYTHOLOGICAL_RITUAL) ->
                "year-$electedYear:mayor:${mayorEntry.stableName()}"
            minister?.perk?.name == MYTHOLOGICAL_RITUAL ->
                "year-$electedYear:minister:${minister.stableName()}"
            else -> null
        }
    }

    private fun MayorEntry.stableName(): String =
        key?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() } ?: "unknown"

    private fun Minister.stableName(): String =
        key?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() } ?: "unknown"

    private data class ElectionResponse(
        val mayor: MayorEntry?,
    )

    private data class MayorEntry(
        val key: String?,
        val name: String?,
        val perks: List<MayorPerk>?,
        val minister: Minister?,
        val election: Election?,
    )

    private data class Minister(
        val key: String?,
        val name: String?,
        val perk: MayorPerk?,
    )

    private data class MayorPerk(
        val name: String?,
    )

    private data class Election(
        val year: Int?,
    )
}
