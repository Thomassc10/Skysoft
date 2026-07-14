package com.skysoft.features.loot

import com.skysoft.config.SkysoftConfigGui
import com.skysoft.data.hypixel.HypixelLocationState
import com.skysoft.utils.SkysoftChat
import com.skysoft.utils.chat.ChatEvents
import com.skysoft.utils.chat.ChatMessage
import com.skysoft.utils.chat.ChatMessageVisibility
import com.skysoft.utils.chat.ChatMessageType
import com.skysoft.utils.chat.SkysoftPartyShare
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

internal object RareLootSharing {
    private val config get() = SkysoftConfigGui.config().misc.rareLootSharing
    private var lastLootShareAtMillis = 0L
    private var lastInvalidThreshold: String? = null

    fun register() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> clear() }
        ChatEvents.onVisibleGameMessageModify(RareLootPartyGlyphRenderer::render)
        ChatEvents.onVisibleMessage { message ->
            onMessage(message)
            ChatMessageVisibility.SHOW
        }
    }

    private fun onMessage(message: ChatMessage) {
        if (!HypixelLocationState.inSkyBlock) return
        val sharingEnabled = config.enabled
        val cleanText = message.cleanText
        val now = System.currentTimeMillis()
        if (sharingEnabled && message.isSystemLike && RareLootShareReceipt.isReceipt(cleanText)) {
            lastLootShareAtMillis = now
        }
        if (!shouldConsiderRareLootMessage(message.type, cleanText)) {
            return
        }

        val chatDrop = RareLootChatParser.parse(cleanText) ?: return
        val lootshare = isLootShareDrop(now)
        val dropCount = RareLootContextRegistry.recordDrop(chatDrop, lootshare, now)
        if (!shouldProcessRareLootMessages(sharingEnabled, HypixelLocationState.inSkyBlock)) return
        shareIfEligible(chatDrop, lootshare, dropCount)
    }

    private fun shareIfEligible(
        chatDrop: RareLootChatDrop,
        lootshare: Boolean,
        dropCount: RareLootDropCount?,
    ) {
        val drop = chatDrop.toRareLootDrop()
        val threshold = currentThreshold() ?: return
        val value = RareLootValueResolver.resolve(drop)
        if (!RareLootEligibility.shouldShare(threshold, value)) return
        SkysoftPartyShare.sendParty(RareLootPartyMessageFormatter.format(drop, value, lootshare, dropCount = dropCount))
    }

    private fun currentThreshold(): RareLootThreshold? =
        when (val result = RareLootThreshold.parse(config.settings.rareLootValue)) {
            is RareLootThresholdParseResult.Valid -> {
                lastInvalidThreshold = null
                result.threshold
            }
            is RareLootThresholdParseResult.Invalid -> {
                warnInvalidThreshold(result)
                null
            }
        }

    private fun warnInvalidThreshold(result: RareLootThresholdParseResult.Invalid) {
        if (lastInvalidThreshold == result.raw) return
        lastInvalidThreshold = result.raw
        SkysoftChat.error("Invalid rare loot value: ${result.raw} (${result.reason})")
    }

    private fun clear() {
        lastLootShareAtMillis = 0L
        lastInvalidThreshold = null
    }

    private fun isLootShareDrop(now: Long): Boolean =
        RareLootContextRegistry.hasLootShareEvidence(now) ||
            RareLootShareReceipt.isWithinWindow(lastLootShareAtMillis, now)

}

internal fun shouldProcessRareLootMessages(enabled: Boolean, inSkyBlock: Boolean): Boolean =
    enabled && inSkyBlock

internal fun shouldConsiderRareLootMessage(
    messageType: ChatMessageType,
    cleanText: String,
): Boolean =
    (messageType == ChatMessageType.SYSTEM || messageType == ChatMessageType.UNKNOWN) &&
        !cleanText.startsWith(PARTY_PREFIX)

private const val PARTY_PREFIX = "Party >"
