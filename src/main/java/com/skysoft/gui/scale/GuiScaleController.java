package com.skysoft.gui.scale;

import com.mojang.blaze3d.platform.Window;
import com.skysoft.config.InventoryScreenConfig;
import com.skysoft.config.SkysoftConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

public final class GuiScaleController {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    private static RenderBatch pendingRenderBatch;
    private static boolean overlaysUseNormalCoordinates;

    private GuiScaleController() {
    }

    public static boolean scalesInventory(Screen screen) {
        return config().separateInventoryGuiScale && supportsInventoryScale(screen);
    }

    public static boolean scalesTooltip(Screen screen) {
        return config().separateTooltipGuiScale && supportsInventoryScale(screen);
    }

    public static ResolvedScales resolve(Screen screen, Window window) {
        int normal = resolve(window, MINECRAFT.options.guiScale().get());
        int tooltip = resolve(window, config().settings.tooltipGuiScale);
        int inventory = Math.min(
            resolve(window, config().settings.inventoryGuiScale),
            inventoryScaleLimit(screen)
        );
        return new ResolvedScales(normal, Math.max(1, inventory), tooltip);
    }

    public static WindowScaleOverride useInventoryScale(Screen screen, Window window) {
        return new WindowScaleOverride(window, resolve(screen, window).inventory());
    }

    public static WindowScaleOverride useTooltipScale(Screen screen, Window window) {
        return new WindowScaleOverride(window, resolve(screen, window).tooltip());
    }

    public static int convertCoordinate(int coordinate, int sourceScale, int targetScale) {
        return Math.round(coordinate * Math.max(1, sourceScale) / (float) Math.max(1, targetScale));
    }

    public static void updateScreenDimensions(Screen screen, Window window) {
        ScaledScreenState state = (ScaledScreenState) screen;
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        if (!state.skysoft$matchesScaleDimensions(width, height)) {
            screen.resize(width, height);
            state.skysoft$rememberScaleDimensions(width, height);
        }
    }

    public static void restoreScreenDimensions(Screen screen, Window window) {
        ScaledScreenState state = (ScaledScreenState) screen;
        if (!state.skysoft$hasScaleDimensions()) {
            return;
        }

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        if (!state.skysoft$matchesScaleDimensions(width, height)) {
            screen.resize(width, height);
        }
        state.skysoft$forgetScaleDimensions();
    }

    public static void submitRenderBatch(GuiRenderState inventory, GuiRenderState overlays) {
        if (pendingRenderBatch != null) {
            throw new IllegalStateException("Inventory GUI render batch was not consumed");
        }
        pendingRenderBatch = new RenderBatch(inventory, overlays);
    }

    public static RenderBatch takeRenderBatch() {
        RenderBatch batch = pendingRenderBatch;
        pendingRenderBatch = null;
        return batch;
    }

    public static boolean overlaysUseNormalCoordinates() {
        return overlaysUseNormalCoordinates;
    }

    public static void setOverlaysUseNormalCoordinates(boolean value) {
        overlaysUseNormalCoordinates = value;
    }

    private static int resolve(Window window, int configuredScale) {
        return Math.max(1, window.calculateScale(Math.max(0, configuredScale), MINECRAFT.isEnforceUnicode()));
    }

    private static boolean supportsInventoryScale(Screen screen) {
        return screen instanceof AbstractContainerScreen<?> ||
            screen instanceof InventoryScaledScreen scaledScreen && scaledScreen.usesInventoryScale();
    }

    private static int inventoryScaleLimit(Screen screen) {
        if (screen instanceof InventoryScaledScreen scaledScreen && scaledScreen.usesInventoryScale()) {
            return Math.max(1, scaledScreen.inventoryScaleLimit());
        }
        return Integer.MAX_VALUE;
    }

    private static InventoryScreenConfig config() {
        return SkysoftConfigGui.INSTANCE.config().gui.inventoryScreen;
    }

    public record ResolvedScales(int normal, int inventory, int tooltip) {
    }

    public record RenderBatch(GuiRenderState inventory, GuiRenderState overlays) {
    }

    public static final class WindowScaleOverride implements AutoCloseable {
        private final Window window;
        private final int previousScale;
        private boolean closed;

        private WindowScaleOverride(Window window, int requestedScale) {
            this.window = window;
            previousScale = window.getGuiScale();
            window.setGuiScale(requestedScale);
        }

        @Override
        public void close() {
            if (closed) {
                throw new IllegalStateException("GUI scale override closed twice");
            }
            closed = true;
            window.setGuiScale(previousScale);
        }
    }
}
