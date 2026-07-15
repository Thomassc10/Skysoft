package com.skysoft.gui.tooltip;

import com.skysoft.config.SkysoftConfigGui;
import com.skysoft.config.TooltipScrollConfig;
import com.skysoft.mixin.ClientTextTooltipAccessor;
import com.skysoft.utils.MinecraftClient;
import com.skysoft.utils.input.InputUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class TooltipViewport {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final long VISIBILITY_GRACE_NANOS = 250_000_000L;
    private static final int EDGE_GAP = 4;
    private static final int ANCHOR_TOLERANCE = 12;

    private static PanSession session;
    private static boolean resetKeyPressedLastTick;

    private TooltipViewport() {
    }

    public static ClientTooltipPositioner decorate(
        Font font,
        List<ClientTooltipComponent> components,
        int anchorX,
        int anchorY,
        ClientTooltipPositioner original
    ) {
        if (!config().enabled || components.isEmpty()) {
            return original;
        }
        return new OffsetPositioner(original, tooltipIdentity(font, components), anchorX, anchorY);
    }

    public static boolean onMouseScroll(double horizontal, double vertical) {
        return onMouseScroll(horizontal, vertical, GLFW.GLFW_KEY_UNKNOWN);
    }

    public static boolean onStorageMouseScroll(double horizontal, double vertical) {
        return onMouseScroll(horizontal, vertical, config().settings.storageOverlayTooltipKey);
    }

    public static boolean isStorageOverlayScrollKeyDown() {
        return isKeyDown(config().settings.storageOverlayTooltipKey);
    }

    private static boolean onMouseScroll(double horizontal, double vertical, int ignoredHorizontalKey) {
        TooltipScrollConfig settings = config();
        if (!settings.enabled || !settings.settings.enableScrollWheel || !hasVisibleSession()) {
            return false;
        }

        boolean pansHorizontally = horizontal != 0.0D || isHorizontalModifierDown(settings, ignoredHorizontalKey);
        double x = horizontal * settings.settings.mouseScrollingSpeed;
        double y = 0.0D;
        if (vertical != 0.0D) {
            if (pansHorizontally) x += vertical * settings.settings.mouseScrollingSpeed;
            else y = vertical * settings.settings.mouseScrollingSpeed;
        }
        if (settings.details.invertHorizontalMovement) x = -x;
        if (settings.details.invertVerticalMovement) y = -y;
        if (x == 0.0D && y == 0.0D) return false;

        session.panBy(x, y);
        return true;
    }

    public static void updateKeyboardPan() {
        TooltipScrollConfig settings = config();
        if (!settings.enabled) {
            clear();
            return;
        }
        if (!hasVisibleSession()) {
            resetKeyPressedLastTick = false;
            if (settings.details.resetPositionWhenNotHovered && session != null) session.center();
            return;
        }

        boolean resetPressed = isKeyDown(settings.settings.resetTooltipKey);
        if (resetPressed && !resetKeyPressedLastTick) session.center();
        resetKeyPressedLastTick = resetPressed;

        int speed = settings.settings.keyboardScrollingSpeed;
        double x = 0.0D;
        double y = 0.0D;
        if (settings.settings.enableWASD) {
            if (isKeyDown(GLFW.GLFW_KEY_A)) x -= speed;
            if (isKeyDown(GLFW.GLFW_KEY_D)) x += speed;
            if (isKeyDown(GLFW.GLFW_KEY_W)) y -= speed;
            if (isKeyDown(GLFW.GLFW_KEY_S)) y += speed;
        }

        boolean horizontal = isHorizontalModifierDown(settings);
        if (isKeyDown(settings.settings.moveUpKey)) {
            if (horizontal) x -= speed;
            else y -= speed;
        }
        if (isKeyDown(settings.settings.moveDownKey)) {
            if (horizontal) x += speed;
            else y += speed;
        }
        if (x != 0.0D || y != 0.0D) session.panBy(x, y);
    }

    public static void clear() {
        session = null;
        resetKeyPressedLastTick = false;
    }

    private static Vector2ic place(
        ClientTooltipPositioner original,
        int identity,
        int anchorX,
        int anchorY,
        int viewportWidth,
        int viewportHeight,
        int x,
        int y,
        int tooltipWidth,
        int tooltipHeight
    ) {
        Vector2ic base = original.positionTooltip(viewportWidth, viewportHeight, x, y, tooltipWidth, tooltipHeight);
        Screen screen = MinecraftClient.screen(MINECRAFT);
        TooltipFrame frame = new TooltipFrame(base.x(), base.y(), tooltipWidth, tooltipHeight, viewportWidth, viewportHeight);
        long now = System.nanoTime();
        boolean expired = !hasVisibleSession(now);
        boolean changedTarget = session == null || session.screen != screen ||
            session.isDifferentTarget(identity, anchorX, anchorY);

        if (session == null || session.screen != screen) {
            session = new PanSession(screen, identity, anchorX, anchorY, frame, now);
        } else {
            session.observe(identity, anchorX, anchorY, frame, now);
            if (config().details.resetPositionWhenNotHovered && (expired || changedTarget)) {
                session.center();
                session.alignTallTooltipToTop();
            }
        }

        session.advance(config().details.scrollSmoothness / 100.0D);
        return new Vector2i(base.x() + session.roundedX(), base.y() + session.roundedY());
    }

    private static boolean hasVisibleSession() {
        return hasVisibleSession(System.nanoTime());
    }

    private static boolean hasVisibleSession(long now) {
        return session != null && session.screen == MinecraftClient.screen(MINECRAFT) &&
            now - session.lastObservedNanos <= VISIBILITY_GRACE_NANOS;
    }

    private static boolean isHorizontalModifierDown(TooltipScrollConfig settings) {
        return isHorizontalModifierDown(settings, GLFW.GLFW_KEY_UNKNOWN);
    }

    private static boolean isHorizontalModifierDown(TooltipScrollConfig settings, int ignoredKey) {
        boolean usesLeftShift = settings.details.useLeftShift && ignoredKey != GLFW.GLFW_KEY_LEFT_SHIFT &&
            isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
        boolean usesConfiguredKey = settings.settings.horizontalMovementKey != ignoredKey &&
            isKeyDown(settings.settings.horizontalMovementKey);
        return usesLeftShift || usesConfiguredKey;
    }

    private static boolean isKeyDown(int key) {
        return InputUtilities.INSTANCE.isKeyDown(key);
    }

    private static int tooltipIdentity(Font font, List<ClientTooltipComponent> components) {
        int result = 1;
        for (ClientTooltipComponent component : components) {
            result = 31 * result + component.getClass().hashCode();
            result = 31 * result + component.getWidth(font);
            result = 31 * result + component.getHeight(font);
            if (component instanceof ClientTextTooltipAccessor textTooltip) {
                result = 31 * result + textIdentity(textTooltip.skysoft$getText());
            }
        }
        return result;
    }

    private static int textIdentity(FormattedCharSequence text) {
        int[] result = {1};
        text.accept((index, style, codePoint) -> {
            result[0] = 31 * result[0] + codePoint;
            result[0] = 31 * result[0] + style.hashCode();
            return true;
        });
        return result[0];
    }

    private static TooltipScrollConfig config() {
        return SkysoftConfigGui.INSTANCE.config().inventory.tooltipScroll;
    }

    private static final class PanSession {
        private final Screen screen;
        private int identity;
        private int anchorX;
        private int anchorY;
        private TooltipFrame frame;
        private long lastObservedNanos;
        private double targetX;
        private double targetY;
        private double displayedX;
        private double displayedY;

        private PanSession(
            Screen screen,
            int identity,
            int anchorX,
            int anchorY,
            TooltipFrame frame,
            long observedAt
        ) {
            this.screen = screen;
            observe(identity, anchorX, anchorY, frame, observedAt);
            alignTallTooltipToTop();
        }

        private boolean isDifferentTarget(int nextIdentity, int nextAnchorX, int nextAnchorY) {
            return identity != nextIdentity || Math.abs(anchorX - nextAnchorX) > ANCHOR_TOLERANCE ||
                Math.abs(anchorY - nextAnchorY) > ANCHOR_TOLERANCE;
        }

        private void observe(
            int nextIdentity,
            int nextAnchorX,
            int nextAnchorY,
            TooltipFrame nextFrame,
            long observedAt
        ) {
            identity = nextIdentity;
            anchorX = nextAnchorX;
            anchorY = nextAnchorY;
            frame = nextFrame;
            lastObservedNanos = observedAt;
            clampMotion();
        }

        private void panBy(double x, double y) {
            targetX += x;
            targetY += y;
            clampMotion();
        }

        private void center() {
            targetX = 0.0D;
            targetY = 0.0D;
            displayedX = 0.0D;
            displayedY = 0.0D;
        }

        private void alignTallTooltipToTop() {
            if (!config().details.startOnTop || frame.height <= frame.viewportHeight - EDGE_GAP * 2 ||
                frame.y >= EDGE_GAP) {
                return;
            }
            targetY = EDGE_GAP - frame.y;
            displayedY = targetY;
            clampMotion();
        }

        private void advance(double amount) {
            if (amount >= 1.0D) {
                displayedX = targetX;
                displayedY = targetY;
                return;
            }
            displayedX = settle(displayedX + (targetX - displayedX) * amount, targetX);
            displayedY = settle(displayedY + (targetY - displayedY) * amount, targetY);
        }

        private void clampMotion() {
            PanBounds bounds = frame.bounds();
            targetX = bounds.clampX(targetX);
            targetY = bounds.clampY(targetY);
            displayedX = bounds.clampX(displayedX);
            displayedY = bounds.clampY(displayedY);
        }

        private int roundedX() {
            return (int) Math.round(displayedX);
        }

        private int roundedY() {
            return (int) Math.round(displayedY);
        }

        private static double settle(double value, double target) {
            return Math.abs(target - value) < 0.05D ? target : value;
        }
    }

    private record TooltipFrame(int x, int y, int width, int height, int viewportWidth, int viewportHeight) {
        private PanBounds bounds() {
            return new PanBounds(
                EDGE_GAP - width - x,
                viewportWidth - EDGE_GAP - x,
                EDGE_GAP - height - y,
                viewportHeight - EDGE_GAP - y
            );
        }
    }

    private record PanBounds(int minX, int maxX, int minY, int maxY) {
        private double clampX(double value) {
            return clamp(value, minX, maxX);
        }

        private double clampY(double value) {
            return clamp(value, minY, maxY);
        }

        private static double clamp(double value, int min, int max) {
            if (min > max) return 0.0D;
            return Math.max(min, Math.min(value, max));
        }
    }

    private record OffsetPositioner(ClientTooltipPositioner original, int identity, int anchorX, int anchorY)
        implements ClientTooltipPositioner {
        @Override
        public Vector2ic positionTooltip(
            int screenWidth,
            int screenHeight,
            int x,
            int y,
            int tooltipWidth,
            int tooltipHeight
        ) {
            return place(original, identity, anchorX, anchorY, screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight);
        }
    }
}
