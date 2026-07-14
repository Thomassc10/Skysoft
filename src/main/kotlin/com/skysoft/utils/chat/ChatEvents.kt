package com.skysoft.utils.chat

import com.skysoft.SkysoftMod
import com.skysoft.utils.SkysoftMessage
import com.skysoft.utils.SkysoftMessageBus
import com.skysoft.utils.SkysoftMessageSource
import com.skysoft.utils.SkysoftMessageVisibility
import net.minecraft.network.chat.Component

object ChatEvents {
    private var visibleListeners: List<(ChatMessage) -> ChatMessageVisibility> = emptyList()
    private var actionBarListeners: List<(SkysoftMessage) -> ChatMessageVisibility> = emptyList()
    private var registered = false

    fun onVisibleMessage(listener: (ChatMessage) -> ChatMessageVisibility) {
        register()
        visibleListeners += listener
    }

    fun onMessageType(messageType: ChatMessageType, listener: (ChatMessage) -> ChatMessageVisibility) {
        onVisibleMessage { message ->
            if (message.type == messageType) listener(message) else ChatMessageVisibility.SHOW
        }
    }

    fun onPartyMessage(listener: (ChatMessage) -> ChatMessageVisibility) {
        onMessageType(ChatMessageType.PARTY, listener)
    }

    fun onActionBar(listener: (SkysoftMessage) -> ChatMessageVisibility) {
        register()
        actionBarListeners += listener
    }

    fun onVisibleGameMessageModify(modifier: (ChatMessage) -> Component) {
        register()
        SkysoftMessageBus.onGameModify { message ->
            if (message.overlay) message.component else modifier(ChatMessageClassifier.classify(message))
        }
    }

    private fun register() {
        if (registered) return
        registered = true
        SkysoftMessageBus.onMessage { message ->
            when {
                message.source == SkysoftMessageSource.GAME && message.overlay -> dispatchActionBar(message)
                !message.overlay -> dispatchVisible(ChatMessageClassifier.classify(message))
                else -> ChatMessageVisibility.SHOW
            }.toSkysoftMessageVisibility()
        }
    }

    private fun dispatchVisible(message: ChatMessage): ChatMessageVisibility =
        dispatch(message, visibleListeners, "Skysoft visible chat listener failed")

    private fun dispatchActionBar(message: SkysoftMessage): ChatMessageVisibility =
        dispatch(message, actionBarListeners, "Skysoft action bar listener failed")

    private fun <T> dispatch(
        message: T,
        listeners: List<(T) -> ChatMessageVisibility>,
        failureMessage: String,
    ): ChatMessageVisibility =
        listeners.fold(ChatMessageVisibility.SHOW) { result, listener ->
            try {
                listener(message).combine(result)
            } catch (exception: Exception) {
                SkysoftMod.LOGGER.error(failureMessage, exception)
                result
            }
        }

    private fun ChatMessageVisibility.combine(previous: ChatMessageVisibility): ChatMessageVisibility =
        if (this == ChatMessageVisibility.HIDE) this else previous

    private fun ChatMessageVisibility.toSkysoftMessageVisibility(): SkysoftMessageVisibility =
        when (this) {
            ChatMessageVisibility.SHOW -> SkysoftMessageVisibility.SHOW
            ChatMessageVisibility.HIDE -> SkysoftMessageVisibility.HIDE
        }
}

enum class ChatMessageVisibility {
    SHOW,
    HIDE,
    ;

    val allowsMessage: Boolean
        get() = this == SHOW
}

enum class ChatMessageType {
    SYSTEM,
    PARTY,
    GUILD,
    COOP,
    PRIVATE_MESSAGE,
    ALL,
    UNKNOWN,
}

enum class PrivateMessageDirection {
    FROM,
    TO,
}

data class ChatMessage(
    val raw: SkysoftMessage,
    val type: ChatMessageType,
    val sender: ChatMessageSender? = null,
    val body: String = raw.cleanText,
    val privateMessageDirection: PrivateMessageDirection? = null,
) {
    val component get() = raw.component
    val source get() = raw.source
    val overlay get() = raw.overlay
    val plainText get() = raw.plainText
    val cleanText get() = raw.cleanText
    val formattedText get() = raw.formattedText
    val cleanFormattedText get() = raw.cleanFormattedText
    val isSystemLike get() = type == ChatMessageType.SYSTEM || type == ChatMessageType.UNKNOWN
}

internal object ChatMessageClassifier {
    fun classify(raw: SkysoftMessage): ChatMessage {
        val clean = raw.cleanText.trim()
        return messageTypePattern(ChatMessageType.PARTY, partyPattern, clean, raw)
            ?: messageTypePattern(ChatMessageType.GUILD, guildPattern, clean, raw)
            ?: messageTypePattern(ChatMessageType.COOP, coopPattern, clean, raw)
            ?: privateMessage(clean, raw)
            ?: allChatMessage(clean, raw)
            ?: ChatMessage(raw = raw, type = ChatMessageType.SYSTEM, body = clean)
    }

    private fun messageTypePattern(
        messageType: ChatMessageType,
        pattern: Regex,
        clean: String,
        raw: SkysoftMessage,
    ): ChatMessage? {
        val match = pattern.matchEntire(clean) ?: return null
        return ChatMessage(
            raw = raw,
            type = messageType,
            sender = senderFromPrefix(match.groups["sender"]?.value.orEmpty()),
            body = match.groups["body"]?.value.orEmpty().trim(),
        )
    }

    private fun senderFromPrefix(prefix: String): ChatMessageSender? {
        val withoutBracketGroups = bracketGroupPattern.replace(prefix, " ")
        val name = withoutBracketGroups
            .split(whitespacePattern)
            .lastOrNull { playerNamePattern.matchEntire(it) != null }
            ?: return null
        return ChatMessageSender(name, null)
    }

    private fun privateMessage(clean: String, raw: SkysoftMessage): ChatMessage? {
        val match = privatePattern.matchEntire(clean)
            ?.takeUnless { clean.startsWith("From stash:", ignoreCase = true) }
            ?: return null
        val direction = when (match.groups["direction"]?.value?.lowercase()) {
            "from" -> PrivateMessageDirection.FROM
            "to" -> PrivateMessageDirection.TO
            else -> null
        }
        return ChatMessage(
            raw = raw,
            type = ChatMessageType.PRIVATE_MESSAGE,
            sender = senderFromPrefix(match.groups["sender"]?.value.orEmpty()),
            body = match.groups["body"]?.value.orEmpty().trim(),
            privateMessageDirection = direction,
        )
    }

    private fun allChatMessage(clean: String, raw: SkysoftMessage): ChatMessage? {
        val match = allPattern.matchEntire(clean) ?: return null
        val sender = senderFromPrefix(match.groups["sender"]?.value.orEmpty()) ?: return null
        return ChatMessage(
            raw = raw,
            type = ChatMessageType.ALL,
            sender = sender,
            body = match.groups["body"]?.value.orEmpty().trim(),
        )
    }

    private val partyPattern = Regex("""^Party > (?<sender>.+?): (?<body>.*)$""")
    private val guildPattern = Regex("""^Guild > (?<sender>.+?): (?<body>.*)$""")
    private val coopPattern = Regex("""^Co-op > (?<sender>.+?): (?<body>.*)$""")
    private val privatePattern = Regex("""^(?<direction>From|To) (?<sender>.+?): (?<body>.*)$""")
    private val allPattern = Regex("""^(?<sender>(?:\[[^]]+] )*[A-Za-z0-9_]{1,16}(?: \[[^]]+])?): (?<body>.*)$""")
    private val bracketGroupPattern = Regex("""\[[^]]+]""")
    private val whitespacePattern = Regex("""\s+""")
    private val playerNamePattern = Regex("""[A-Za-z0-9_]{1,16}""")
}
