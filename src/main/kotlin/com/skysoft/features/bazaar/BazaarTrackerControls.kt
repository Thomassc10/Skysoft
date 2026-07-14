package com.skysoft.features.bazaar

import com.skysoft.gui.OverlayControlMouse
import com.skysoft.gui.tooltip.SkysoftNativeTooltip
import com.skysoft.utils.SoundUtilities
import com.skysoft.utils.input.InputHandlingResult
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.lwjgl.glfw.GLFW

internal fun handleBazaarTrackerMouseButtonPress(button: Int): InputHandlingResult {
    if (!shouldRenderBazaarTrackerInventoryOverlay()) return InputHandlingResult.IGNORED
    if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
        return InputHandlingResult.IGNORED
    }
    return handleTrackerControlClick(button)
}

internal fun renderTrackerControlTooltip(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
    val area = hoveredControlArea ?: return
    val (tooltipMouseX, tooltipMouseY) = OverlayControlMouse.deferredTooltipPoint(mouseX, mouseY)
    SkysoftNativeTooltip.setForNextFrame(context, area.tooltipLines, tooltipMouseX, tooltipMouseY)
}

internal fun handleTrackerControlClick(button: Int): InputHandlingResult {
    val area = hoveredControlArea ?: return InputHandlingResult.IGNORED
    val activated = when (area.action) {
        TrackerControl.TOGGLE_MODE -> {
            cycleDisplayMode(backwards = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            true
        }
        TrackerControl.RESET -> if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            resetBazaarTrackerDisplayedProfit()
            true
        } else {
            false
        }
    }
    if (activated) SoundUtilities.playClickSound()
    return InputHandlingResult.CONSUMED
}
