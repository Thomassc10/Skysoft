package com.skysoft.features.event.diana

import com.skysoft.features.loot.RareLootValueResolver
import com.skysoft.utils.NumberUtilities.addSeparators
import com.skysoft.utils.NumberUtilities.coinFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

internal object MythologicalRitualPartyCommands {
    private val commandMap: Map<String, (MythologicalRitualTrackerState) -> String> =
        buildMap {
            add(listOf("!chim", "!chimera", "!chims", "!chimeras", "!book", "!books")) { state ->
                state.fmt("Chimera", MythologicalRitualItemKey.CHIMERA, MythologicalRitualMobKey.MINOS_INQUISITOR) +
                    " +${state.event.item(MythologicalRitualItemKey.CHIMERA_LS)} LS"
            }
            add(listOf("!inqsls", "!inquisitorls", "!inquisls", "!lsinq", "!lsinqs", "!lsinquisitor", "!lsinquis")) {
                "Inquisitor LS: ${it.event.mob(MythologicalRitualMobKey.MINOS_INQUISITOR_LS)}"
            }
            add(listOf("!inq", "!inqs", "!inquisitor", "!inquis")) {
                it.fmt("Inquisitor", MythologicalRitualMobKey.MINOS_INQUISITOR)
            }
            add(listOf("!king", "!kings", "!kingminos", "!minosking")) {
                it.fmt("King Minos", MythologicalRitualMobKey.KING_MINOS)
            }
            add(listOf("!kingls", "!kingsls", "!lsking", "!lskings", "!kingminosls", "!lskingminos")) {
                "King Minos LS: ${it.event.mob(MythologicalRitualMobKey.KING_MINOS_LS)}"
            }
            add(listOf("!burrows", "!burrow")) { "Burrows: ${it.event.burrows()} (${it.event.burrowsPerHour()}/h)" }
            add(listOf("!relic", "!relics")) {
                it.fmt("Relics", MythologicalRitualItemKey.MINOS_RELIC, MythologicalRitualMobKey.MINOS_CHAMPION)
            }
            add(listOf("!chimls", "!chimerals", "!bookls", "!lschim", "!lsbook", "!lootsharechim", "!lschimera")) {
                it.fmt("Chimera LS", MythologicalRitualItemKey.CHIMERA_LS, MythologicalRitualMobKey.MINOS_INQUISITOR_LS)
            }
            add(listOf("!core", "!manticore")) {
                it.fmt("Cores", MythologicalRitualItemKey.MANTI_CORE, MythologicalRitualMobKey.MANTICORE)
            }
            add(listOf("!corels", "!manticorels", "!lscore", "!lsmanticore")) {
                it.fmt("Core LS", MythologicalRitualItemKey.MANTI_CORE_LS, MythologicalRitualMobKey.MANTICORE_LS)
            }
            add(listOf("!stinger", "!fatefulstinger")) {
                it.fmt("Stingers", MythologicalRitualItemKey.FATEFUL_STINGER, MythologicalRitualMobKey.MANTICORE)
            }
            add(listOf("!stingerls", "!fatefulstingerls", "!lsstinger", "!lsfatefulstinger")) {
                it.fmt("Stinger LS", MythologicalRitualItemKey.FATEFUL_STINGER_LS, MythologicalRitualMobKey.MANTICORE_LS)
            }
            addBasicDropCommands()
        }

    fun response(body: String, localPlayerName: String?, state: MythologicalRitualTrackerState): String? {
        val parts = body.trim().split(Regex("""\s+""")).filter { it.isNotBlank() }
        val command = parts.firstOrNull()?.lowercase(Locale.US) ?: return null
        val secondArg = parts.getOrNull(1)
        if (parts.size > 1 && command !in commandsWithArgs) return null
        return when (command) {
            "!stats", "!stat" -> statsResponse(secondArg, localPlayerName, state.event, state)
            "!totalstats", "!totalstat" -> statsResponse(secondArg, localPlayerName, state.total, state)
            "!sessionstats", "!sessionstat" -> statsResponse(secondArg, localPlayerName, state.session, state)
            "!since" -> sinceResponse(secondArg, state.since)
            else -> commandMap[command]?.invoke(state)
        }
    }

    private fun MutableMap<String, (MythologicalRitualTrackerState) -> String>.addBasicDropCommands() {
        add(listOf("!wool", "!wools", "!shimmering", "!shimmeringwool", "!shimmeringwools")) {
            it.fmt("Wool", MythologicalRitualItemKey.SHIMMERING_WOOL, MythologicalRitualMobKey.KING_MINOS)
        }
        add(listOf("!woolls", "!shimmeringwoolls", "!lswool", "!lswools", "!lsshimmering", "!lsshimmeringwool")) {
            it.fmt("Wool LS", MythologicalRitualItemKey.SHIMMERING_WOOL_LS, MythologicalRitualMobKey.KING_MINOS_LS)
        }
        add(listOf("!food", "!brainfood", "!brain")) {
            it.fmt("Brain Food", MythologicalRitualItemKey.BRAIN_FOOD, MythologicalRitualMobKey.SPHINX)
        }
        add(listOf("!foodls", "!brainfoodls", "!lsbrainfood", "!lsbrain")) {
            it.fmt("Brain Food LS", MythologicalRitualItemKey.BRAIN_FOOD_LS, MythologicalRitualMobKey.SPHINX_LS)
        }
        addShardAndTreasureCommands()
        add(listOf("!mf", "!magicfind")) { it.magicFindResponse() }
        add(listOf("!playtime")) { "Playtime: ${it.event.activeMillis.formatTime()}" }
        add(listOf("!profits", "!profit")) { it.profitResponse() }
    }

    private fun MutableMap<String, (MythologicalRitualTrackerState) -> String>.addShardAndTreasureCommands() {
        add(listOf("!kingshard", "!kingshards")) {
            it.fmt("King Shards", MythologicalRitualItemKey.KING_MINOS_SHARD, MythologicalRitualMobKey.KING_MINOS)
        }
        add(listOf("!sphinxshard", "!sphinxshards")) {
            it.fmt("Sphinx Shards", MythologicalRitualItemKey.SPHINX_SHARD, MythologicalRitualMobKey.SPHINX)
        }
        add(listOf("!minotaurshard", "!minotaurshards")) {
            it.fmt("Minotaur Shards", MythologicalRitualItemKey.MINOTAUR_SHARD, MythologicalRitualMobKey.MINOTAUR)
        }
        add(listOf("!certanshard", "!certanshards")) {
            it.fmt("Certan Shards", MythologicalRitualItemKey.CRETAN_BULL_SHARD, MythologicalRitualMobKey.CRETAN_BULL)
        }
        add(listOf("!mythofrag", "!frags")) { "Mytho Frags: ${it.event.item(MythologicalRitualItemKey.MYTHOS_FRAGMENT)}" }
        add(listOf("!urns", "!urn", "!cretanurn")) {
            it.fmt("Urns", MythologicalRitualItemKey.CRETAN_URN, MythologicalRitualMobKey.CRETAN_BULL)
        }
        add(listOf("!hilt", "!hiltofrevelations")) {
            it.fmt("Hilts", MythologicalRitualItemKey.HILT_OF_REVELATIONS, MythologicalRitualMobKey.MINOS_HUNTER)
        }
        add(listOf("!sticks", "!stick")) {
            it.fmt("Sticks", MythologicalRitualItemKey.DAEDALUS_STICK, MythologicalRitualMobKey.MINOTAUR)
        }
        add(listOf("!feathers", "!feather")) { "Feathers: ${it.event.item(MythologicalRitualItemKey.GRIFFIN_FEATHER)}" }
        add(listOf("!coins", "!coin")) { "Coins: ${it.event.item(MythologicalRitualItemKey.COINS).addSeparators()}" }
        add(listOf("!mobs", "!mob")) { "Mobs: ${it.event.mob(MythologicalRitualMobKey.TOTAL_MOBS)} (${it.event.mobsPerHour()}/h)" }
    }

    private fun MutableMap<String, (MythologicalRitualTrackerState) -> String>.add(
        aliases: List<String>,
        response: (MythologicalRitualTrackerState) -> String,
    ) {
        aliases.forEach { alias -> put(alias, response) }
    }

    private fun MythologicalRitualTrackerState.fmt(label: String, key: String, denominatorMob: String? = null): String {
        val count = if (denominatorMob == null) event.mob(key) else event.item(key)
        val denominator = denominatorMob?.let(event::mob) ?: event.mob(MythologicalRitualMobKey.TOTAL_MOBS)
        val percent = if (denominator <= 0L) 0.0 else count.toDouble() / denominator.toDouble() * PERCENT_MULTIPLIER
        return "$label: $count (${percent.percentFormat()}%)"
    }

    private fun statsResponse(
        targetPlayer: String?,
        localPlayerName: String?,
        stats: MythologicalRitualStats,
        state: MythologicalRitualTrackerState,
    ): String? {
        if (targetPlayer == null || !targetPlayer.equals(localPlayerName, ignoreCase = true)) return null
        return listOf(
            "Playtime: ${stats.activeMillis.formatTime()}",
            "Profit: ${state.profit(stats).coinFormat()}",
            "Burrows: ${stats.burrows()}",
            "Mobs: ${stats.mob(MythologicalRitualMobKey.TOTAL_MOBS)}",
            "Inquisitors: ${stats.mob(MythologicalRitualMobKey.MINOS_INQUISITOR)}",
            "LS Inqs: ${stats.mob(MythologicalRitualMobKey.MINOS_INQUISITOR_LS)}",
            "Kings: ${stats.mob(MythologicalRitualMobKey.KING_MINOS)}",
            "LS Kings: ${stats.mob(MythologicalRitualMobKey.KING_MINOS_LS)}",
            "Chimeras: ${stats.item(MythologicalRitualItemKey.CHIMERA)}",
            "LS: ${stats.item(MythologicalRitualItemKey.CHIMERA_LS)}",
            "Sticks: ${stats.item(MythologicalRitualItemKey.DAEDALUS_STICK)}",
            "Relics: ${stats.item(MythologicalRitualItemKey.MINOS_RELIC)}",
        ).joinToString(" | ")
    }

    private fun sinceResponse(arg: String?, since: MythologicalRitualSinceData): String =
        when (arg?.lowercase(Locale.US)) {
            "chimera", "chim", "chims", "chimeras", "book", "books" -> "Inqs since chim: ${since.inqsSinceChim}"
            "stick", "sticks" -> "Minos since stick: ${since.minotaursSinceStick}"
            "relic", "relics" -> "Champs since relic: ${since.champsSinceRelic}"
            "inq", "inqs", "inquisitor", "inquisitors", "inquis" -> "Mobs since inq: ${since.mobsSinceInq}"
            "lschim", "chimls", "lschimera", "chimerals", "lsbook", "bookls", "lootsharechim" ->
                "Inqs since lootshare chim: ${since.inqsSinceLsChim}"
            "kings", "king", "kingminos", "minosking" -> "Mobs since king: ${since.mobsSinceKing}"
            "manti" -> "Mobs since manti: ${since.mobsSinceManti}"
            "core", "cores" -> "Mantis since core: ${since.mantiSinceCore}"
            "wool", "wools" -> "Kings since wool: ${since.kingSinceWool}"
            "corels", "lscore" -> "Mantis since lootshare core: ${since.mantiSinceLsCore}"
            "woolls", "lswool" -> "Kings since lootshare wool: ${since.kingSinceLsWool}"
            else -> "Mobs since inq: ${since.mobsSinceInq}"
        }

    private fun MythologicalRitualTrackerState.magicFindResponse(): String =
        "Wool (${magicFind.get(MythologicalRitualMagicFindKey.WOOL)}% ✯) " +
            "Manticore (${magicFind.get(MythologicalRitualMagicFindKey.CORE)}% ✯) " +
            "Stinger (${magicFind.get(MythologicalRitualMagicFindKey.STINGER)}% ✯) " +
            "Chim (${magicFind.get(MythologicalRitualMagicFindKey.CHIMERA)}% ✯) " +
            "Relic (${magicFind.get(MythologicalRitualMagicFindKey.RELIC)}% ✯) " +
            "Food (${magicFind.get(MythologicalRitualMagicFindKey.FOOD)}% ✯) " +
            "Stick (${magicFind.get(MythologicalRitualMagicFindKey.STICK)}% ✯)"

    private fun MythologicalRitualTrackerState.profitResponse(): String {
        val profit = profit(event)
        val perHour = event.perHour(profit)
        return "Profit: ${profit.coinFormat()} (Current Prices) ${perHour.coinFormat()}/h"
    }

    private fun MythologicalRitualTrackerState.profit(stats: MythologicalRitualStats): Double {
        val itemProfit = stats.items.entries.sumOf { (itemKey, amount) ->
            if (itemKey == MythologicalRitualItemKey.COINS) {
                amount.toDouble()
            } else {
                val itemId = MythologicalRitualDropMapping.priceItemId(itemKey) ?: return@sumOf 0.0
                RareLootValueResolver.resolve(itemId, amount.toInt())?.coins ?: 0.0
            }
        }
        return itemProfit.coerceAtLeast(0.0)
    }

    private fun MythologicalRitualStats.burrows(): Long =
        item(MythologicalRitualItemKey.TOTAL_BURROWS)

    private fun MythologicalRitualStats.burrowsPerHour(): String =
        perHour(burrows().toDouble()).rateFormat()

    private fun MythologicalRitualStats.mobsPerHour(): String =
        perHour(mob(MythologicalRitualMobKey.TOTAL_MOBS).toDouble()).rateFormat()

    private fun MythologicalRitualStats.perHour(value: Double): Double {
        val hours = activeMillis.toDouble() / TimeUnit.HOURS.toMillis(1).toDouble()
        return if (hours <= 0.0) 0.0 else value / hours
    }

    private fun Double.percentFormat(): String =
        String.format(Locale.US, "%.2f", this)

    private fun Double.rateFormat(): String =
        String.format(Locale.US, "%.2f", this)

    private fun Long.formatTime(): String {
        if (this <= 0L) return "0s"
        val totalSeconds = (this / MILLIS_PER_SECOND).toInt()
        val totalMinutes = totalSeconds / SECONDS_PER_MINUTE
        val totalHours = totalMinutes / MINUTES_PER_HOUR
        val days = totalHours / HOURS_PER_DAY
        val hours = totalHours % HOURS_PER_DAY
        val minutes = totalMinutes % MINUTES_PER_HOUR
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return buildList {
            if (days > 0) add("${days}d")
            if (hours > 0 || days > 0) add("${hours}h")
            if (minutes > 0 || hours > 0 || days > 0) add("${minutes}m")
            if (isEmpty()) add("${seconds}s")
        }.joinToString(" ")
    }

    private val commandsWithArgs = setOf(
        "!since",
        "!stats",
        "!stat",
        "!totalstats",
        "!totalstat",
        "!sessionstats",
        "!sessionstat",
    )
    private const val PERCENT_MULTIPLIER = 100.0
    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60
    private const val MINUTES_PER_HOUR = 60
    private const val HOURS_PER_DAY = 24
}
