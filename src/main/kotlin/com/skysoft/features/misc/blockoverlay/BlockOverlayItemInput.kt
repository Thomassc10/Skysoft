package com.skysoft.features.misc.blockoverlay

import com.skysoft.utils.TextUtilities.cleanSkyBlockText
import java.util.Locale

object BlockOverlayItemInput {
    fun resolve(isEmpty: Boolean, itemId: String?, canonicalName: String?): BlockOverlayItemInputResult {
        if (isEmpty) return BlockOverlayItemInputResult.Rejected(BlockOverlayItemRejection.EMPTY_HAND)
        val normalizedId = itemId?.trim()?.uppercase(Locale.US)?.takeIf { it.isNotEmpty() }
            ?: return BlockOverlayItemInputResult.Rejected(BlockOverlayItemRejection.MISSING_ID)
        val cleanName = canonicalName?.cleanSkyBlockText()?.takeIf { it.isNotEmpty() }
            ?: return BlockOverlayItemInputResult.Rejected(BlockOverlayItemRejection.NAME_UNAVAILABLE)
        return BlockOverlayItemInputResult.Ready(normalizedId, cleanName)
    }
}

sealed interface BlockOverlayItemInputResult {
    data class Ready(val itemId: String, val cleanName: String) : BlockOverlayItemInputResult
    data class Rejected(val reason: BlockOverlayItemRejection) : BlockOverlayItemInputResult
}

enum class BlockOverlayItemRejection {
    EMPTY_HAND,
    MISSING_ID,
    NAME_UNAVAILABLE,
}
