package com.skysoft.features.loot

import com.skysoft.utils.NumberUtilities.parseCompactNumberOrNull

internal data class RareLootThreshold(
    val coins: Double,
) {
    companion object {
        fun parse(raw: String): RareLootThresholdParseResult {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return RareLootThresholdParseResult.Invalid(raw, "Enter a coin value.")
            val parsed = trimmed.parseCompactNumberOrNull()?.value
                ?: return RareLootThresholdParseResult.Invalid(raw, "Use a number like 1m or 1,000,000.")
            if (parsed.isNaN() || parsed.isInfinite()) {
                return RareLootThresholdParseResult.Invalid(raw, "Use a finite coin value.")
            }
            if (parsed < 0.0) {
                return RareLootThresholdParseResult.Invalid(raw, "Use zero or more coins.")
            }
            return RareLootThresholdParseResult.Valid(RareLootThreshold(parsed))
        }
    }
}

internal sealed interface RareLootThresholdParseResult {
    data class Valid(val threshold: RareLootThreshold) : RareLootThresholdParseResult
    data class Invalid(val raw: String, val reason: String) : RareLootThresholdParseResult
}
