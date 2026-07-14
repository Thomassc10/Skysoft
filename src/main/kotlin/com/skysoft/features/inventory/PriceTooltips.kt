package com.skysoft.features.inventory

import com.skysoft.config.BazaarPriceType
import com.skysoft.config.SkysoftConfigGui
import com.skysoft.data.skyblock.SkyBlockItemId.skyBlockId
import com.skysoft.data.skyblock.price.SkyBlockPriceData
import com.skysoft.utils.NumberUtilities.coinAmountFormat
import com.skysoft.utils.input.InputUtilities
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object PriceTooltips {
    private val config get() = SkysoftConfigGui.config().inventory.priceTooltips

    fun register() {
        ItemTooltipCallback.EVENT.register { stack, _, _, tooltip ->
            if (!shouldShow()) return@register

            val itemId = stack.skyBlockId() ?: return@register
            val priceLines = createPriceLines(itemId)
            if (priceLines.isEmpty()) return@register

            tooltip.add(Component.literal(""))
            tooltip.addAll(priceLines)
        }
    }

    private fun shouldShow(): Boolean {
        if (!config.enabled) return false
        if (!config.settings.requireKey) return true
        return InputUtilities.isKeyDown(config.settings.hotkey)
    }

    private fun createPriceLines(itemId: String): List<Component> = buildList {
        SkyBlockPriceData.getBazaarPrice(itemId)?.let { bazaar ->
            when (config.settings.bazaarPriceType) {
                BazaarPriceType.ORDER_PRICES -> {
                    addPrice("Bazaar Buy Order", bazaar.buyOrderPrice)
                    addPrice("Bazaar Sell Order", bazaar.sellOrderPrice)
                }

                BazaarPriceType.INSTANT_PRICES -> {
                    addPrice("Bazaar Instant Buy", bazaar.instantBuyPrice)
                    addPrice("Bazaar Instant Sell", bazaar.instantSellPrice)
                }
            }
        }

        SkyBlockPriceData.getLowestBin(itemId)?.let { lowestBin ->
            add(formatPrice("Lowest BIN", lowestBin.toDouble()))
        }
    }

    private fun MutableList<Component>.addPrice(label: String, price: Double) {
        if (price <= 0.0) return
        add(formatPrice(label, price))
    }

    private fun formatPrice(label: String, price: Double): Component =
        Component.literal("")
            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
            .append(Component.literal(label))
            .append(": ")
            .append(
                Component.literal("${price.coinAmountFormat()} ${if (price == 1.0) "coin" else "coins"}")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            )
}
