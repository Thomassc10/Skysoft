package com.skysoft.features.event.diana

import com.skysoft.config.SkysoftConfigFiles
import com.skysoft.data.hypixel.SkyBlockProfileId
import com.skysoft.data.hypixel.SkyBlockProfileApi
import com.skysoft.data.skyblock.MayorPerkApi

internal object MythologicalRitualTrackerRepository {
    private val store = MythologicalRitualTrackerStore(SkysoftConfigFiles.mythologicalRitualTracker)
    private val sessionStats = mutableMapOf<SkyBlockProfileId, MythologicalRitualStats>()
    private var data = MythologicalRitualTrackerData()
    private var loaded = false
    private var lastKnownProfileKey: String? = null
    private var lastSaveAtMillis = 0L

    fun state(): MythologicalRitualTrackerState {
        ensureLoaded()
        val playerKey = SkyBlockProfileApi.currentPlayerKey()
        val profileKey = currentProfileKey()
        val eventKey = currentEventKey()
        val profile = data.profile(playerKey, profileKey)
        if (eventKey != UNRESOLVED_EVENT_KEY) {
            profile.mergeEvent(UNRESOLVED_EVENT_KEY, eventKey)
        }
        return MythologicalRitualTrackerState(
            event = profile.event(eventKey),
            total = profile.total,
            session = sessionStats.getOrPut(SkyBlockProfileId(playerKey, profileKey)) { MythologicalRitualStats() },
            since = profile.since,
            magicFind = profile.magicFind,
        )
    }

    fun update(now: Long = System.currentTimeMillis(), action: (MythologicalRitualTrackerState) -> Unit) {
        val trackerState = state()
        action(trackerState)
        saveNow(now)
    }

    fun recordActiveAt(now: Long) {
        val trackerState = state()
        trackerState.event.recordActiveAt(now)
        trackerState.total.recordActiveAt(now)
        trackerState.session.recordActiveAt(now)
        if (now - lastSaveAtMillis >= ACTIVE_SAVE_INTERVAL_MILLIS) saveNow(now)
    }

    fun saveNow(now: Long = System.currentTimeMillis()) {
        if (!loaded) return
        store.save(data)
        lastSaveAtMillis = now
    }

    private fun ensureLoaded() {
        if (loaded) return
        data = store.load()
        loaded = true
    }

    private fun currentProfileKey(): String {
        SkyBlockProfileApi.currentProfileKey?.let { profile ->
            lastKnownProfileKey = profile
            return profile
        }
        return lastKnownProfileKey ?: UNKNOWN_PROFILE_KEY
    }

    private fun currentEventKey(): String =
        MayorPerkApi.mythologicalRitualEventKey ?: UNRESOLVED_EVENT_KEY

    private const val UNKNOWN_PROFILE_KEY = "unknown-profile"
    private const val UNRESOLVED_EVENT_KEY = "unresolved-current-event"
    private const val ACTIVE_SAVE_INTERVAL_MILLIS = 30_000L
}
