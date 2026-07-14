package com.skysoft.features.loot

internal object RareLootShareReceipt {
    fun target(message: String): String? =
        receiptPattern.matchEntire(message.trim())?.groups?.get("target")?.value

    fun isReceipt(message: String): Boolean =
        target(message) != null

    fun isWithinWindow(lastReceiptAtMillis: Long, now: Long): Boolean =
        lastReceiptAtMillis > 0L && now - lastReceiptAtMillis <= RECEIPT_WINDOW_MILLIS

    private val receiptPattern = Regex(
        """^LOOT SHARE You received(?: .+?)? for assisting (?<target>.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private const val RECEIPT_WINDOW_MILLIS = 2_000L
}
