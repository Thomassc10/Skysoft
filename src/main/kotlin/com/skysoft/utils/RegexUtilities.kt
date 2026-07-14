package com.skysoft.utils

object RegexUtilities {
    fun MatchResult.group(name: String): String =
        groups[name]?.value.orEmpty()

    fun MatchResult.groupOrNull(name: String): String? =
        runCatching { groups[name]?.value }.getOrNull()
}
