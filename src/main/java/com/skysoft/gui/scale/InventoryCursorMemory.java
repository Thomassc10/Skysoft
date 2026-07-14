package com.skysoft.gui.scale;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.skysoft.config.SkysoftConfigGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class InventoryCursorMemory {
    private static final long EXPIRY_NANOS = 1_000_000_000L;
    private static final double CENTER_TOLERANCE = 1.0;
    private static final int RESTORE_ATTEMPTS = 3;

    private static CursorPoint cursorBeforeGrab;
    private static CursorSnapshot snapshot;
    private static int remainingRestores;

    private InventoryCursorMemory() {
    }

    public static void rememberScreenCursor(Screen screen, double x, double y) {
        if (!canRemember(screen)) {
            discard();
            return;
        }
        snapshot = new CursorSnapshot(new CursorPoint(x, y), null, System.nanoTime());
        remainingRestores = 0;
    }

    public static void beginMouseGrab(double x, double y) {
        if (!isEnabled()) {
            discard();
            return;
        }
        cursorBeforeGrab = new CursorPoint(x, y);
    }

    public static void finishMouseGrab(double centerX, double centerY) {
        if (!isEnabled()) {
            discard();
            return;
        }
        if (cursorBeforeGrab == null) {
            return;
        }
        snapshot = new CursorSnapshot(cursorBeforeGrab, new CursorPoint(centerX, centerY), System.nanoTime());
        cursorBeforeGrab = null;
    }

    public static CursorPoint cursorForRelease(double centerX, double centerY) {
        if (!isUsable() || snapshot.grabCenter() == null) {
            return null;
        }
        CursorPoint expected = snapshot.grabCenter();
        if (Math.abs(expected.x() - centerX) >= CENTER_TOLERANCE ||
            Math.abs(expected.y() - centerY) >= CENTER_TOLERANCE) {
            discard();
            return null;
        }

        remainingRestores = RESTORE_ATTEMPTS;
        snapshot = new CursorSnapshot(snapshot.cursor(), null, snapshot.capturedAt());
        return snapshot.cursor();
    }

    public static void restoreWhenScreenInitializes(Screen screen, Window window, CursorController cursor) {
        if (!canRestore(screen)) {
            return;
        }
        remainingRestores = RESTORE_ATTEMPTS;
        restoreOnce(window, cursor);
    }

    public static void continueRestore(Screen screen, Window window, CursorController cursor) {
        if (remainingRestores <= 0) {
            return;
        }
        if (!canRestore(screen)) {
            return;
        }
        restoreOnce(window, cursor);
    }

    private static void restoreOnce(Window window, CursorController cursor) {
        CursorPoint point = snapshot.cursor();
        cursor.skysoft$moveCursor(point.x(), point.y());
        InputConstants.grabOrReleaseMouse(window, InputConstants.CURSOR_NORMAL, point.x(), point.y());
        remainingRestores--;
        if (remainingRestores == 0) {
            discard();
        }
    }

    private static boolean canRemember(Screen screen) {
        return isEnabled() && screen instanceof AbstractContainerScreen<?>;
    }

    private static boolean canRestore(Screen screen) {
        if (!canRemember(screen)) {
            discard();
            return false;
        }
        if (!isUsable()) {
            discard();
            return false;
        }
        return true;
    }

    private static boolean isUsable() {
        return snapshot != null && System.nanoTime() - snapshot.capturedAt() <= EXPIRY_NANOS;
    }

    private static boolean isEnabled() {
        return SkysoftConfigGui.INSTANCE.config().inventory.preserveCursorPosition;
    }

    private static void discard() {
        cursorBeforeGrab = null;
        snapshot = null;
        remainingRestores = 0;
    }

    private record CursorSnapshot(CursorPoint cursor, CursorPoint grabCenter, long capturedAt) {
    }

    public record CursorPoint(double x, double y) {
    }
}
