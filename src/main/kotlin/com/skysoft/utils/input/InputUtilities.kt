package com.skysoft.utils.input

import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

object InputUtilities {
    fun isKeyDown(key: Int): Boolean {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return false
        val window = Minecraft.getInstance().window.handle()
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS
    }

    fun isShiftDown(): Boolean = isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)

    fun clipboardAscii(): String = Minecraft.getInstance().keyboardHandler.clipboard.filter { it.code in 32..126 }
}
