package com.skysoft.features.event.diana

import com.skysoft.utils.NumberUtilities.parseCompactNumberOrNull

internal data class DianaMobHealth(
    val current: Long,
    val max: Long?,
)

internal object DianaMobTextParsers {
    private val healthPattern = Regex("""(?<current>[0-9,.]+[KMBkmb]?)(?:§.)?\s*/\s*(?<max>[0-9,.]+[KMBkmb]?)""")
    private val currentHealthPattern = Regex("""(?<current>[0-9,.]+[KMBkmb]?)(?:§.)?❤""")

    fun parseHealth(name: String): DianaMobHealth? {
        val fullHealth = healthPattern.find(name)?.let { match ->
            val current = match.groups["current"]?.value?.parseCompactNumber()
            val max = match.groups["max"]?.value?.parseCompactNumber()
            if (current != null && max != null) DianaMobHealth(current, max) else null
        }
        if (fullHealth != null) return fullHealth

        return currentHealthPattern.find(name)?.let { match ->
            match.groups["current"]?.value?.parseCompactNumber()?.let { current ->
                DianaMobHealth(current, null)
            }
        }
    }

    private fun String.parseCompactNumber(): Long? {
        val value = replace(",", "").trim()
        if (value.isEmpty()) return null
        return value.parseCompactNumberOrNull()?.value?.toLong()
    }
}
