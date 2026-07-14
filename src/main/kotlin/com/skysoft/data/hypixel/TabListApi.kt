package com.skysoft.data.hypixel

import com.skysoft.utils.TextUtilities.cleanSkyBlockText
import com.skysoft.utils.ElapsedTimeMark
import com.skysoft.utils.TabListFooter
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.PlayerTeam
import java.util.Collections
import java.util.UUID
import kotlin.time.Duration

object TabListApi {
    private const val SKYBLOCK_DATA_READY_READS = 2
    private val skyBlockAreaPattern = Regex("""Area: .+""")

    private var cachedLines: List<Component> = emptyList()
    private var cachedEntries: List<TabListEntry> = emptyList()
    private var cachedFooter: Component? = null
    private var loaded = false
    private var skyBlockDataLoaded = false
    private var skyBlockDataReads = 0
    private var lastLocationVersion = Long.MIN_VALUE
    private var ticks = 0
    private var skyBlockDataLoadStartedAt = ElapsedTimeMark.farPast()

    var sessionId: Long = 0
        private set

    var contentVersion: Long = 0
        private set

    val isLoaded: Boolean
        get() = HypixelLocationState.inSkyBlock && loaded

    val lines: List<Component>
        get() = if (isLoaded) cachedLines else emptyList()

    val entries: List<TabListEntry>
        get() = if (isLoaded) cachedEntries else emptyList()

    val footer: Component?
        get() = if (isLoaded) cachedFooter else null

    val isSkyBlockDataLoaded: Boolean
        get() = isLoaded && skyBlockDataLoaded

    val skyBlockLines: List<Component>
        get() = if (isSkyBlockDataLoaded) cachedLines else emptyList()

    val skyBlockFooter: Component?
        get() = if (isSkyBlockDataLoaded) cachedFooter else null

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register {
            onClientTick()
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            resetSession()
        }
    }

    fun hasWaitedForSkyBlockData(duration: Duration): Boolean =
        HypixelLocationState.inSkyBlock && skyBlockDataLoadStartedAt.passedSince() >= duration

    private fun onClientTick() {
        if (!HypixelLocationState.inSkyBlock) {
            ticks = 0
            return
        }

        if (lastLocationVersion != HypixelLocationState.locationVersion) {
            resetSession()
            lastLocationVersion = HypixelLocationState.locationVersion
        }

        if (++ticks % READ_INTERVAL_TICKS != 0) return
        val nextEntries = readTabList()
        if (nextEntries.isEmpty()) {
            clearLoadedLines()
            return
        }

        val nextLines = nextEntries.map { it.displayName }
        val nextFooter = TabListFooter.read(Minecraft.getInstance())
        if (nextLines != cachedLines || nextFooter != cachedFooter) contentVersion++
        cachedEntries = Collections.unmodifiableList(nextEntries)
        cachedLines = Collections.unmodifiableList(nextLines)
        cachedFooter = nextFooter
        loaded = true
        updateSkyBlockDataLoadState(nextLines)
    }

    private fun resetSession() {
        sessionId++
        if (cachedLines.isNotEmpty()) contentVersion++
        cachedLines = emptyList()
        cachedEntries = emptyList()
        cachedFooter = null
        loaded = false
        skyBlockDataLoaded = false
        skyBlockDataReads = 0
        ticks = 0
        skyBlockDataLoadStartedAt = ElapsedTimeMark.now()
    }

    private fun clearLoadedLines() {
        if (!loaded) return
        contentVersion++
        cachedLines = emptyList()
        cachedEntries = emptyList()
        cachedFooter = null
        loaded = false
        resetSkyBlockDataLoad()
    }

    private fun updateSkyBlockDataLoadState(lines: List<Component>) {
        if (!hasSkyBlockData(lines)) {
            resetSkyBlockDataLoad()
            return
        }
        skyBlockDataReads = (skyBlockDataReads + 1).coerceAtMost(SKYBLOCK_DATA_READY_READS)
        skyBlockDataLoaded = skyBlockDataReads >= SKYBLOCK_DATA_READY_READS
    }

    private fun resetSkyBlockDataLoad() {
        if (skyBlockDataLoaded || skyBlockDataReads > 0) {
            skyBlockDataLoadStartedAt = ElapsedTimeMark.now()
        }
        skyBlockDataLoaded = false
        skyBlockDataReads = 0
    }

    private fun hasSkyBlockData(lines: List<Component>): Boolean {
        val cleanLines = lines.map { it.cleanSkyBlockText() }
        return cleanLines.any { it == "Info" } && cleanLines.any { skyBlockAreaPattern.matches(it) }
    }

    private fun readTabList(): List<TabListEntry> {
        val connection = Minecraft.getInstance().connection ?: return emptyList()
        return connection.listedOnlinePlayers.map {
            TabListEntry(
                uuid = it.profile.id,
                profileName = it.profile.name,
                displayName = it.tabListDisplayName
                    ?: PlayerTeam.formatNameForTeam(it.team, Component.literal(it.profile.name)),
            )
        }
    }

    private const val READ_INTERVAL_TICKS = 5
}

data class TabListEntry(
    val uuid: UUID,
    val profileName: String,
    val displayName: Component,
)
