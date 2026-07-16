package com.skysoft.gui.scale

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.platform.Window
import com.skysoft.config.SkysoftConfigGui
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

class InventoryCursorMemory private constructor() {
    @JvmRecord
    data class CursorPoint(val x: Double, val y: Double)

    private data class CursorSnapshot(
        val cursor: CursorPoint,
        val grabCenter: CursorPoint?,
        val capturedAt: Long,
    )

    companion object {
        private const val EXPIRY_NANOS = 1_000_000_000L
        private const val CENTER_TOLERANCE = 1.0
        private const val RESTORE_ATTEMPTS = 3

        private var cursorBeforeGrab: CursorPoint? = null
        private var snapshot: CursorSnapshot? = null
        private var remainingRestores = 0

        @JvmStatic
        fun rememberScreenCursor(screen: Screen?, x: Double, y: Double) {
            if (!canRemember(screen)) {
                discard()
                return
            }
            snapshot = CursorSnapshot(CursorPoint(x, y), null, System.nanoTime())
            remainingRestores = 0
        }

        @JvmStatic
        fun beginMouseGrab(x: Double, y: Double) {
            if (!isEnabled()) {
                discard()
                return
            }
            cursorBeforeGrab = CursorPoint(x, y)
        }

        @JvmStatic
        fun finishMouseGrab(centerX: Double, centerY: Double) {
            if (!isEnabled()) {
                discard()
                return
            }
            val cursor = cursorBeforeGrab ?: return
            snapshot = CursorSnapshot(cursor, CursorPoint(centerX, centerY), System.nanoTime())
            cursorBeforeGrab = null
        }

        @JvmStatic
        fun cursorForRelease(centerX: Double, centerY: Double): CursorPoint? {
            if (!isUsable()) return null
            val current = checkNotNull(snapshot)
            val expected = current.grabCenter ?: return null
            if (
                kotlin.math.abs(expected.x - centerX) >= CENTER_TOLERANCE ||
                kotlin.math.abs(expected.y - centerY) >= CENTER_TOLERANCE
            ) {
                discard()
                return null
            }
            remainingRestores = RESTORE_ATTEMPTS
            snapshot = CursorSnapshot(current.cursor, null, current.capturedAt)
            return current.cursor
        }

        @JvmStatic
        fun restoreWhenScreenInitializes(screen: Screen?, window: Window, cursor: CursorController) {
            if (!canRestore(screen)) return
            remainingRestores = RESTORE_ATTEMPTS
            restoreOnce(window, cursor)
        }

        @JvmStatic
        fun continueRestore(screen: Screen?, window: Window, cursor: CursorController) {
            if (remainingRestores <= 0 || !canRestore(screen)) return
            restoreOnce(window, cursor)
        }

        private fun restoreOnce(window: Window, cursor: CursorController) {
            val point = checkNotNull(snapshot).cursor
            cursor.skysoftMoveCursor(point.x, point.y)
            InputConstants.grabOrReleaseMouse(window, InputConstants.CURSOR_NORMAL, point.x, point.y)
            remainingRestores--
            if (remainingRestores == 0) discard()
        }

        private fun canRemember(screen: Screen?): Boolean =
            isEnabled() && screen is AbstractContainerScreen<*>

        private fun canRestore(screen: Screen?): Boolean {
            if (!canRemember(screen) || !isUsable()) {
                discard()
                return false
            }
            return true
        }

        private fun isUsable(): Boolean =
            snapshot?.let { System.nanoTime() - it.capturedAt <= EXPIRY_NANOS } == true

        private fun isEnabled(): Boolean = SkysoftConfigGui.config().inventory.preserveCursorPosition

        private fun discard() {
            cursorBeforeGrab = null
            snapshot = null
            remainingRestores = 0
        }
    }
}
