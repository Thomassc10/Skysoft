package com.skysoft.features.inventory

import com.skysoft.data.hypixel.SkyBlockProfileApi
import com.skysoft.mixin.AbstractContainerScreenAccessor
import com.skysoft.utils.input.InputHandlingResult
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

internal fun registerStorageOverlay() {
    SkyBlockProfileApi.onProfileChange { resetTransientState() }
    ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> resetTransientState() }
    ClientTickEvents.END_CLIENT_TICK.register {
        onClientTick()
    }
    registerMouseClickInterceptor()
}

internal fun registerMouseClickInterceptor() {
    ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
        if (screen is AbstractContainerScreen<*>) {
            ScreenMouseEvents.allowMouseClick(screen).register { _, click ->
                handlePreScreenMouseClick(screen, click) == InputHandlingResult.IGNORED
            }
        }
    }
}

internal fun storageOverlayIsActive(screen: AbstractContainerScreen<*>?): Boolean = handleFor(screen) != null

internal fun storageOverlayLayoutScreen(screen: AbstractContainerScreen<*>) {
    val handle = handleFor(screen) ?: run {
        restoreStorageOverlaySlots(screen)
        return
    }
    readScreen(screen, handle)
    rememberActivePage(handle)
    redirectToRememberedPage(screen, handle)

    val measurements = measurements(screen.width, screen.height)
    val activePage = handle.entryIndex()
    var pageLayoutResult = pageLayouts(measurements, activePage)
    if (centerActivePageIfNeeded(activePage, measurements, pageLayoutResult) == PageLayoutRefresh.REQUIRED) {
        pageLayoutResult = pageLayouts(measurements, activePage)
    }
    val pageLayouts = pageLayoutResult.pages

    val accessor = screen as AbstractContainerScreenAccessor
    val left = accessor.`skysoft$getLeftPos`()
    val top = accessor.`skysoft$getTopPos`()
    val playerInventory = Minecraft.getInstance().player?.inventory
    for (slot in screen.menu.slots) {
        val position = when {
            playerInventory != null && slot.container === playerInventory ->
                playerSlotPosition(measurements, slot.containerSlot)

            handle.gridRows() != null ->
                pageSlotPosition(
                    measurements,
                    handle,
                    handle.entryIndex()?.let { pageLayouts[it] },
                    slot,
                )

            else -> null
        }
        moveStorageOverlaySlot(
            screen,
            slot,
            position?.x?.minus(left) ?: StorageRuntime.OFFSCREEN,
            position?.y?.minus(top) ?: StorageRuntime.OFFSCREEN,
        )
    }
}

internal fun renderStorageOverlayBackground(
    screen: ContainerScreen,
    context: GuiGraphicsExtractor,
    mouseX: Int,
    mouseY: Int,
) {
    if (!storageOverlayIsActive(screen)) return
    renderOverlay(screen, context, mouseX, mouseY)
}

internal fun renderOverlay(screen: ContainerScreen, context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
    val handle = handleFor(screen) ?: return
    readScreen(screen, handle)
    storageOverlayLayoutScreen(screen)
    val measurements = measurements(screen.width, screen.height)
    val activePage = handle.entryIndex()
    val pageLayoutResult = pageLayouts(measurements, activePage)

    drawStoragePanel(context, measurements)
    drawPages(
        context,
        screen,
        measurements,
        pageLayoutResult.pages,
        handle.takeIf { it.gridRows() != null },
        mouseX,
        mouseY,
    )
    drawScrollBar(context, measurements, pageLayoutResult.contentHeight)
    drawSearchBox(context, measurements)
    drawPlayerInventoryPanel(context, screen, measurements, mouseX, mouseY)
    if (config.settings.miniMenu) drawStorageSelectorPanel(context, measurements, activePage, mouseX, mouseY)
    drawCarriedItem(context, screen, mouseX, mouseY)
    coerceScroll(measurements, pageLayoutResult.contentHeight)
}

internal fun shouldSuppressStorageOverlayContainerLabels(screen: AbstractContainerScreen<*>): Boolean =
    storageOverlayIsActive(screen)

internal fun handlePreScreenMouseClick(
    screen: AbstractContainerScreen<*>,
    click: MouseButtonEvent,
): InputHandlingResult {
    val handle = handleFor(screen) ?: return InputHandlingResult.IGNORED
    readScreen(screen, handle)
    storageOverlayLayoutScreen(screen)

    if (routeActivePageSlotClick(screen, handle, click) == InputHandlingResult.CONSUMED) {
        return InputHandlingResult.CONSUMED
    }

    return handleStorageOverlayMouseClick(screen, click)
}

internal fun handleStorageOverlayMouseClick(
    screen: AbstractContainerScreen<*>,
    click: MouseButtonEvent,
): InputHandlingResult {
    val handle = handleFor(screen) ?: return InputHandlingResult.IGNORED
    readScreen(screen, handle)
    storageOverlayLayoutScreen(screen)
    val measurements = measurements(screen.width, screen.height)
    val activePage = handle.entryIndex()
    val pageLayoutResult = pageLayouts(measurements, activePage)
    val mouseX = click.x().toInt()
    val mouseY = click.y().toInt()

    return when {
        pointInSearch(measurements, mouseX, mouseY) -> {
            searchFocused = true
            finishTitleEdit()
            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && searchText.isNotEmpty()) {
                searchText = ""
                coerceScroll(measurements, pageLayouts(measurements, activePage).contentHeight)
            }
            InputHandlingResult.CONSUMED
        }
        click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && measurements.scrollPanel.contains(mouseX, mouseY) -> {
            val titlePage = titlePageAt(pageLayoutResult.pages, mouseX, mouseY)
            if (titlePage != null) {
                startTitleEdit(titlePage)
                searchFocused = false
                InputHandlingResult.CONSUMED
            } else {
                finishTitleEdit()
                handlePageAreaClick(screen, click, measurements, pageLayoutResult.pages, activePage, mouseX, mouseY)
            }
        }
        config.settings.miniMenu &&
            handleSelectorPageClick(screen, click, measurements, pageLayoutResult, activePage, mouseX, mouseY) ==
            InputHandlingResult.CONSUMED -> InputHandlingResult.CONSUMED
        config.settings.miniMenu && measurements.selectorBounds.contains(mouseX, mouseY) -> InputHandlingResult.CONSUMED
        measurements.playerBounds.contains(mouseX, mouseY) -> InputHandlingResult.IGNORED
        else -> {
            searchFocused = false
            finishTitleEdit()
            handlePageAreaClick(
                screen,
                click,
                measurements,
                pageLayoutResult.pages,
                activePage,
                mouseX,
                mouseY,
            )
        }
    }
}

internal fun handleSelectorPageClick(
    screen: AbstractContainerScreen<*>,
    click: MouseButtonEvent,
    measurements: Measurements,
    pageLayoutResult: PageLayoutResult,
    activePage: Int?,
    mouseX: Int,
    mouseY: Int,
): InputHandlingResult {
    val pageIndex = selectorPageAt(measurements, mouseX, mouseY) ?: return InputHandlingResult.IGNORED
    if (pageIndex == activePage || !screen.menu.carried.isEmpty || click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        centerPage(measurements, pageLayoutResult, pageIndex)
    } else {
        tryNavigateTo(screen, pageIndex, mouseX, mouseY)
    }
    storageOverlayLayoutScreen(screen)
    return InputHandlingResult.CONSUMED
}

internal fun handlePageAreaClick(
    screen: AbstractContainerScreen<*>,
    click: MouseButtonEvent,
    measurements: Measurements,
    layouts: Map<Int, PageLayout>,
    activePage: Int?,
    mouseX: Int,
    mouseY: Int,
): InputHandlingResult {
    val clickedPage = if (measurements.scrollPanel.contains(mouseX, mouseY)) {
        layouts.values.firstOrNull { it.contains(mouseX, mouseY) }
    } else {
        null
    } ?: return if (measurements.totalBounds.contains(mouseX, mouseY)) {
        InputHandlingResult.CONSUMED
    } else {
        InputHandlingResult.IGNORED
    }

    return if (clickedPage.pageIndex != activePage && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        if (screen.menu.carried.isEmpty) tryNavigateTo(screen, clickedPage.pageIndex, mouseX, mouseY)
        InputHandlingResult.CONSUMED
    } else {
        InputHandlingResult.IGNORED
    }
}

internal fun handleStorageOverlayMouseScroll(
    screen: AbstractContainerScreen<*>,
    mouseX: Double,
    mouseY: Double,
    scrollY: Double,
): InputHandlingResult {
    val handle = handleFor(screen) ?: return InputHandlingResult.IGNORED
    val measurements = measurements(screen.width, screen.height)
    if (!measurements.scrollPanel.contains(mouseX.toInt(), mouseY.toInt())) return InputHandlingResult.IGNORED
    scroll -= (scrollY * config.details.scrollSpeed).roundToInt()
    val activePage = handle.entryIndex()
    coerceScroll(measurements, pageLayouts(measurements, activePage).contentHeight)
    storageOverlayLayoutScreen(screen)
    return InputHandlingResult.CONSUMED
}

internal fun handleStorageOverlayKeyPress(screen: AbstractContainerScreen<*>, event: KeyEvent): InputHandlingResult {
    if (!storageOverlayIsActive(screen)) return InputHandlingResult.IGNORED
    if (editingTitlePage != null) return handleTitleEditKeyPress(screen, event)
    if (!searchFocused) return InputHandlingResult.IGNORED
    when (event.key()) {
        GLFW.GLFW_KEY_ESCAPE -> {
            if (searchText.isEmpty()) {
                searchFocused = false
            } else {
                searchText = ""
                scroll = 0
                storageOverlayLayoutScreen(screen)
            }
            return InputHandlingResult.CONSUMED
        }

        GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
            searchFocused = false
            return InputHandlingResult.CONSUMED
        }

        GLFW.GLFW_KEY_BACKSPACE -> {
            if (searchText.isNotEmpty()) {
                searchText = searchText.dropLast(1)
                scroll = 0
                storageOverlayLayoutScreen(screen)
            }
            return InputHandlingResult.CONSUMED
        }
    }
    val typed = when (event.key()) {
        GLFW.GLFW_KEY_SPACE -> " "
        else -> GLFW.glfwGetKeyName(event.key(), event.scancode())
    }
    val blockedModifiers = GLFW.GLFW_MOD_CONTROL or GLFW.GLFW_MOD_ALT or GLFW.GLFW_MOD_SUPER
    if (typed?.length == 1 && (event.modifiers() and blockedModifiers) == 0) {
        searchText += typed
        scroll = 0
        storageOverlayLayoutScreen(screen)
    }
    return InputHandlingResult.CONSUMED
}

internal fun isStorageOverlayClickInside(
    screen: AbstractContainerScreen<*>,
    mouseX: Double,
    mouseY: Double,
): Boolean {
    if (!storageOverlayIsActive(screen)) return false
    val measurements = measurements(screen.width, screen.height)
    return measurements.totalBounds.contains(mouseX.toInt(), mouseY.toInt()) ||
        measurements.playerBounds.contains(mouseX.toInt(), mouseY.toInt()) ||
        (config.settings.miniMenu && measurements.selectorBounds.contains(mouseX.toInt(), mouseY.toInt()))
}
