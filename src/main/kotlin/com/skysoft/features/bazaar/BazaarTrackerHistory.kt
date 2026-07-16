package com.skysoft.features.bazaar

import com.skysoft.data.ProfileStorage
import com.skysoft.utils.gui.nonPlayerInventoryKey
import com.skysoft.utils.gui.nonPlayerSlotAt
import com.skysoft.utils.gui.nonPlayerSlots as screenNonPlayerSlots
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

internal fun resetTransientState(resetSessionStats: Boolean) {
    pendingSetup = null
    pendingOrderOptionId = null
    pendingCancel = null
    lastOrdersInventoryKey = null
    pendingOrdersInventoryKey = null
    pendingOrdersInventoryStableTicks = 0
    hoveredControlArea = null
    statusAlertTick = 0
    lastOrdersGuiClickMillis = 0L
    lastOrdersGuiClickSignature = ""
    lastAlertStatuses.clear()
    lastOutbidAlertMillis.clear()
    missingFromOrdersGuiScans.clear()
    fillHighlightExpiresAt.clear()
    marketProofMillis.clear()
    fillEstimateStates.clear()
    depthRefreshTick = 0
    recentResolvedOrders.clear()
    if (resetSessionStats) {
        sessionKnownProfit = 0.0
        sessionBuySetupValue = 0.0
        sessionSellSetupValue = 0.0
    }
}

internal fun rememberResolvedOrder(order: ProfileStorage.BazaarOrderData) {
    forgetOrderAlertState(order.id)
    fillHighlightExpiresAt.remove(order.id)
    marketProofMillis.remove(order.id)
    fillEstimateStates.remove(order.id)
    pruneRecentResolvedOrders()
    recentResolvedOrders.addLast(
        RecentResolvedOrder(
            type = order.type,
            itemName = order.itemName,
            productId = order.productId,
            amount = order.amountOrdered,
            totalCoins = order.totalCoins,
            timestampMillis = System.currentTimeMillis(),
        ),
    )
    while (recentResolvedOrders.size > MAX_RECENT_RESOLVED_ORDERS) recentResolvedOrders.removeFirst()
}

internal fun pruneRecentResolvedOrders() {
    val cutoff = System.currentTimeMillis() - RECENT_RESOLVED_SUPPRESS_MILLIS
    while (recentResolvedOrders.firstOrNull()?.timestampMillis?.let { it < cutoff } == true) recentResolvedOrders.removeFirst()
}

internal fun buildInventoryKey(screen: AbstractContainerScreen<*>): String = screen.nonPlayerInventoryKey()

internal fun nonPlayerSlots(screen: AbstractContainerScreen<*>): List<Slot> = screen.screenNonPlayerSlots()

internal fun slotAt(screen: AbstractContainerScreen<*>, mouseX: Int, mouseY: Int): Slot? = screen.nonPlayerSlotAt(mouseX, mouseY)

