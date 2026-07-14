package com.skysoft.features.bazaar

import com.skysoft.data.ProfileStorageApi
import com.skysoft.data.ProfileStorage
import kotlin.math.max

internal fun addActiveOrder(
    order: ProfileStorage.BazaarOrderData,
    requireFreshMarketProof: Boolean,
) {
    storage.activeOrders += order
    recordBazaarTransaction(storage, order.toBazaarTransaction())
    if (requireFreshMarketProof) requireMarketProof(order)
    initializeOrderAlertState(order)
}

internal fun markBazaarTrackerChanged(
    refreshFillEstimates: Boolean = false,
    refreshOrderBook: Boolean = false,
) {
    ProfileStorageApi.markDirty()
    if (refreshFillEstimates) requestBazaarFillEstimateRefresh()
    if (refreshOrderBook) BazaarOrderBookApi.refreshNow()
}

internal fun clearPendingOrderAction() {
    pendingCancel = null
    pendingOrderOptionId = null
}

internal fun applyClaimedAmount(
    order: ProfileStorage.BazaarOrderData,
    amount: Long,
    claimedCoins: Double = 0.0,
    alert: Boolean = true,
): OrderRemovalResult {
    val previousFilled = order.filledAmount
    order.filledAmount = max(order.filledAmount, order.claimedAmount + amount)
    if (alert) playProgressAlert(order, previousFilled)
    order.claimedAmount += amount
    order.claimedCoins += claimedCoins
    order.updatedAtMillis = System.currentTimeMillis()
    val removed = order.claimedAmount >= order.maximumAmount()
    if (removed) {
        rememberResolvedOrder(order)
        storage.activeOrders.remove(order)
    }
    return if (removed) OrderRemovalResult.REMOVED else OrderRemovalResult.KEPT
}

internal enum class OrderRemovalResult {
    REMOVED,
    KEPT,
}
