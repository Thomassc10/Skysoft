package com.skysoft.utils.gui

import com.skysoft.utils.input.InputHandlingResult
import com.skysoft.utils.input.InputUtilities
import com.skysoft.utils.ColorUtilities.withScaledAlpha
import com.skysoft.utils.render.LegacyTextRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

internal class TextFieldState(var text: String = "", val maxLength: Int = 256) {
    var focused = false

    fun render(
        context: GuiGraphicsExtractor,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        placeholder: String,
        prefix: String = "",
        alpha: Double = 1.0,
        outlineColor: Int? = null,
    ) {
        context.fill(x, y, x + width, y + height, BACKGROUND_COLOR.withScaledAlpha(alpha))
        context.outline(
            x,
            y,
            width,
            height,
            (outlineColor ?: if (focused) FOCUSED_OUTLINE_COLOR else OUTLINE_COLOR).withScaledAlpha(alpha),
        )
        val displayText = if (text.isEmpty() && !focused) "§8$placeholder" else "§f$prefix$text"
        LegacyTextRenderer.draw(
            context,
            displayText.takeLast(VISIBLE_TEXT_LENGTH),
            x + TEXT_X_OFFSET,
            y + TEXT_Y_OFFSET,
            shadow = false,
            defaultColor = TEXT_COLOR.withScaledAlpha(alpha),
        )
        if (focused && (System.currentTimeMillis() / CURSOR_BLINK_MILLIS) % CURSOR_BLINK_PHASES == 0L) {
            val cursorText = (prefix + text).takeLast(VISIBLE_TEXT_LENGTH)
            val cursorX = x + TEXT_X_OFFSET + Minecraft.getInstance().font.width(cursorText)
            context.fill(
                cursorX,
                y + CURSOR_Y_INSET,
                cursorX + CURSOR_WIDTH,
                y + height - CURSOR_Y_INSET,
                TEXT_COLOR.withScaledAlpha(alpha),
            )
        }
    }

    fun keyPressed(event: KeyEvent): InputHandlingResult {
        val control = event.modifiers() and GLFW.GLFW_MOD_CONTROL != 0
        return when (event.key()) {
            GLFW.GLFW_KEY_BACKSPACE -> {
                if (text.isNotEmpty()) {
                    text = if (control) textAfterDeletingPreviousWord(text) else text.dropLast(1)
                }
                InputHandlingResult.CONSUMED
            }
            GLFW.GLFW_KEY_DELETE -> {
                text = ""
                InputHandlingResult.CONSUMED
            }
            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                focused = false
                InputHandlingResult.CONSUMED
            }
            GLFW.GLFW_KEY_V -> if (control) pasteClipboard() else InputHandlingResult.IGNORED
            GLFW.GLFW_KEY_A -> {
                if (control) {
                    text = ""
                    InputHandlingResult.CONSUMED
                } else {
                    InputHandlingResult.IGNORED
                }
            }
            else -> InputHandlingResult.IGNORED
        }
    }

    fun charTyped(event: CharacterEvent) {
        append(event.codepointAsString())
    }

    private fun pasteClipboard(): InputHandlingResult {
        append(InputUtilities.clipboardAscii())
        return InputHandlingResult.CONSUMED
    }

    private fun append(value: String) {
        if (value.isEmpty()) return
        text = (text + value).take(maxLength)
    }

    private companion object {
        const val VISIBLE_TEXT_LENGTH = 32
        const val TEXT_X_OFFSET = 5
        const val TEXT_Y_OFFSET = 5
        const val CURSOR_BLINK_MILLIS = 500L
        const val CURSOR_BLINK_PHASES = 2L
        const val CURSOR_Y_INSET = 4
        const val CURSOR_WIDTH = 1

        val BACKGROUND_COLOR = 0xFF080808.toInt()
        val FOCUSED_OUTLINE_COLOR = 0xFF55FFFF.toInt()
        val OUTLINE_COLOR = 0xFF505050.toInt()
        val TEXT_COLOR = 0xFFFFFFFF.toInt()
    }
}

internal fun textAfterDeletingPreviousWord(text: String): String {
    val wordEnd = text.indexOfLast { !it.isWhitespace() } + 1
    if (wordEnd == 0) return ""
    val wordStart = text.substring(0, wordEnd).indexOfLast(Char::isWhitespace) + 1
    return text.substring(0, wordStart)
}
