package com.skysoft.features.bazaar

import com.skysoft.data.skyblock.price.SkyBlockPriceData

object BazaarOrderBookApi {
    fun get(productId: String?): BazaarMarket? {
        if (productId == null) return null
        val product = SkyBlockPriceData.getBazaarProduct(productId) ?: return null
        if (product.bestBuyOrder <= 0.0 || product.bestSellOrder <= 0.0) return null
        return BazaarMarket(
            productId = productId,
            bestBuyOrder = product.bestBuyOrder,
            bestSellOrder = product.bestSellOrder,
            buyOrderAmount = product.buyOrderAmount,
            sellOrderAmount = product.sellOrderAmount,
            updatedAtMillis = SkyBlockPriceData.getBazaarUpdatedAtMillis(),
        )
    }

    fun refreshNow() = SkyBlockPriceData.refreshBazaarNow()
}

data class BazaarMarket(
    val productId: String,
    val bestBuyOrder: Double,
    val bestSellOrder: Double,
    val buyOrderAmount: Long,
    val sellOrderAmount: Long,
    val updatedAtMillis: Long,
)
