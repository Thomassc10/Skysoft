package com.skysoft.features.loot

import com.skysoft.data.skyblock.price.BazaarPriceData
import com.skysoft.data.skyblock.price.SkyBlockPriceData

internal object RareLootValueResolver {
    fun resolve(drop: RareLootDrop): RareLootValue? =
        resolve(drop.itemId, drop.amount)

    fun resolve(
        itemId: String?,
        amount: Int = 1,
        bazaarPrice: (String) -> BazaarPriceData? = SkyBlockPriceData::getBazaarPrice,
        lowestBin: (String) -> Long? = SkyBlockPriceData::getLowestBin,
    ): RareLootValue? {
        val cleanItemId = itemId?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val multiplier = amount.coerceAtLeast(1).toDouble()
        bazaarPrice(cleanItemId)?.let { bazaar ->
            bazaar.sellOrderPrice()?.let { price ->
                return RareLootValue(price * multiplier, RareLootValueSource.BAZAAR_SELL_ORDER)
            }
            bazaar.instantSellPrice()?.let { price ->
                return RareLootValue(price * multiplier, RareLootValueSource.BAZAAR_INSTANT_SELL)
            }
        }
        lowestBin(cleanItemId)?.takeIf { it > 0L }?.let { price ->
            return RareLootValue(price.toDouble() * multiplier, RareLootValueSource.LOWEST_BIN)
        }
        return null
    }

    private fun BazaarPriceData.sellOrderPrice(): Double? =
        sellOrderPrice.takeIf { it > 0.0 }

    private fun BazaarPriceData.instantSellPrice(): Double? =
        instantSellPrice.takeIf { it > 0.0 }
}

internal data class RareLootValue(
    val coins: Double,
    val source: RareLootValueSource,
)

internal enum class RareLootValueSource {
    BAZAAR_SELL_ORDER,
    BAZAAR_INSTANT_SELL,
    LOWEST_BIN,
}
