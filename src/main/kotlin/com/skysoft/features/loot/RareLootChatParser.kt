package com.skysoft.features.loot

import com.skysoft.data.skyblock.SkyBlockStatGlyph
import com.skysoft.utils.NumberUtilities.romanNumeral
import com.skysoft.utils.NumberUtilities.romanToDecimal
import java.util.Locale

internal object RareLootChatParser {
    fun parse(message: String): RareLootChatDrop? {
        val clean = message.trim()
        if (clean.startsWith(PARTY_PREFIX)) return null
        dugOutPattern.matchEntire(clean)?.let { return parseDugOut(it) }
        val directDrop = directRareDropPattern.matchEntire(clean) ?: return null
        return parseDirectRareDrop(directDrop.groups["drop"]?.value.orEmpty().trim())
    }

    private fun parseDugOut(match: MatchResult): RareLootChatDrop? {
        val name = cleanDropName(match.groups["drop"]?.value.orEmpty()) ?: return null
        val amountDrop = parseDrop(name)
        return RareLootChatDrop(
            displayName = amountDrop.displayName,
            amount = amountDrop.amount,
            context = null,
            itemIdCandidates = amountDrop.itemIdCandidates,
        )
    }

    private fun parseDirectRareDrop(body: String): RareLootChatDrop? {
        val (dropText, context) = splitContext(body)
        val name = cleanDropName(dropText) ?: return null
        val amountDrop = parseDrop(name)
        return RareLootChatDrop(
            displayName = amountDrop.displayName,
            amount = amountDrop.amount,
            context = context,
            itemIdCandidates = amountDrop.itemIdCandidates,
        )
    }

    private fun splitContext(body: String): Pair<String, String?> {
        val match = contextPattern.matchEntire(body)
        val rawContext = match?.groups?.get("context")?.value
        return if (match == null || rawContext == null) {
            body to null
        } else {
            match.groups["drop"]?.value.orEmpty().trim() to rawContext.normalizeContext()
        }
    }

    private fun cleanDropName(text: String): String? {
        val clean = text.trim()
            .removeSuffix("!")
            .trim()
            .removeSurrounding("(", ")")
            .trim()
            .removePrefix("a ")
            .removePrefix("an ")
            .trim()
        if (clean.isBlank()) return null
        if (clean.equals("Griffin Burrow", ignoreCase = true)) return null
        if (clean.contains("coin", ignoreCase = true)) return null
        return clean
    }

    private fun parseDrop(name: String): ParsedChatDrop {
        val amountDrop = parseAmount(name)
        return parseEnchantedBook(amountDrop.displayName)?.copy(amount = amountDrop.amount) ?: amountDrop
    }

    private fun parseAmount(name: String): ParsedChatDrop {
        val match = amountPattern.matchEntire(name) ?: return ParsedChatDrop(name, 1)
        val amount = match.groups["amount"]?.value?.toIntOrNull()?.coerceAtLeast(1) ?: return ParsedChatDrop(name, 1)
        val displayName = match.groups["drop"]?.value?.trim()?.takeIf { it.isNotEmpty() } ?: return ParsedChatDrop(name, 1)
        return ParsedChatDrop(displayName, amount)
    }

    private fun parseEnchantedBook(name: String): ParsedChatDrop? {
        val match = enchantedBookPattern.matchEntire(name) ?: return null
        val enchantName = match.groups["enchant"]?.value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val level = match.groups["level"]?.value?.parseEnchantLevel() ?: return null
        val enchantKey = enchantName.enchantmentKey()
        return ParsedChatDrop(
            displayName = "${enchantName.titleCaseWords()} ${level.romanNumeral()}",
            amount = 1,
            itemIdCandidates = listOf(
                "ENCHANTMENT_${enchantKey}_$level",
                "ENCHANTMENT_ULTIMATE_${enchantKey}_$level",
            ).distinct(),
        )
    }

    private fun String.normalizeContext(): String =
        trim()
            .removePrefix("(")
            .removeSuffix(")")
            .trim()
            .let(SkyBlockStatGlyph::normalizeForRendering)
            .replace(Regex("""Magic Find""", RegexOption.IGNORE_CASE), "MF")

    private fun String.parseEnchantLevel(): Int? =
        toIntOrNull() ?: uppercase(Locale.US).romanToDecimal().takeIf { it > 0 }

    private fun String.enchantmentKey(): String =
        uppercase(Locale.US)
            .replace(Regex("""[^A-Z0-9]+"""), "_")
            .trim('_')

    private fun String.titleCaseWords(): String =
        lowercase(Locale.US)
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.US) } }

    private const val PARTY_PREFIX = "Party >"
    private val dugOutPattern = Regex(
        """^(?:(?:(?:VERY|CRAZY)\s+)?RARE DROP!\s+|Wow!\s+)?You dug out(?: an? )?(?<drop>.+?)!(?: .*)?$""",
        RegexOption.IGNORE_CASE,
    )
    private val directRareDropPattern = Regex(
        """^(?:(?:VERY|CRAZY)\s+)?RARE DROP!\s+(?<drop>.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private val contextPattern = Regex(
        """^(?<drop>.+?)\s+(?<context>\([^)]*(?:Magic Find|MF)[^)]*\))(?:\s+.*)?$""",
        RegexOption.IGNORE_CASE,
    )
    private val amountPattern = Regex("""^(?<amount>\d+)\s*x\s+(?<drop>.+)$""", RegexOption.IGNORE_CASE)
    private val enchantedBookPattern = Regex("""^Enchanted Book \((?<enchant>.+?) (?<level>\d+|[IVXLCDM]+)\)$""", RegexOption.IGNORE_CASE)
}

internal data class RareLootChatDrop(
    val displayName: String,
    val amount: Int,
    val context: String?,
    val itemIdCandidates: List<String> = emptyList(),
)

private data class ParsedChatDrop(
    val displayName: String,
    val amount: Int,
    val itemIdCandidates: List<String> = emptyList(),
)
