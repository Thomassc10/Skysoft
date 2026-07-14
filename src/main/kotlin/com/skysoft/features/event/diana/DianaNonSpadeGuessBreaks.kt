package com.skysoft.features.event.diana

import com.skysoft.data.InteractionClick
import com.skysoft.events.input.BlockInteractionEvent

internal object DianaNonSpadeGuessBreaks {
    private val breakGrace = mutableMapOf<Long, Long>()

    fun onBlockClick(
        event: BlockInteractionEvent,
        target: DianaBurrowTarget,
        now: Long,
    ): DianaBlockClickResult {
        if (event.clickType != InteractionClick.LEFT_CLICK || target.source != DianaBurrowSource.GUESS) {
            return DianaBlockClickResult.ALLOW
        }
        breakGrace[target.targetId] = now + NON_SPADE_GUESS_BREAK_GRACE_MILLIS
        return DianaBlockClickResult.CANCEL
    }

    fun hasRecentBreakAttempt(target: DianaBurrowTarget, now: Long): Boolean {
        prune(now)
        return breakGrace[target.targetId]?.let { expiresAt -> now < expiresAt } == true
    }

    fun onTick(now: Long) {
        prune(now)
    }

    fun clear() {
        breakGrace.clear()
    }

    private fun prune(now: Long) {
        breakGrace.entries.removeIf { (_, expiresAt) -> now >= expiresAt }
    }

    private const val NON_SPADE_GUESS_BREAK_GRACE_MILLIS = 2_000L
}

internal enum class DianaBlockClickResult(
    val shouldCancel: Boolean,
) {
    ALLOW(false),
    CANCEL(true),
}
