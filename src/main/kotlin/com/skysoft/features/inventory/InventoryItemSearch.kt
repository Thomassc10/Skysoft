package com.skysoft.features.inventory

import com.skysoft.data.skyblock.SkyBlockItemUtilities.formattedHoverName
import com.skysoft.data.skyblock.SkyBlockItemUtilities.loreLines
import com.skysoft.utils.TextUtilities.cleanSkyBlockText
import java.lang.ref.WeakReference
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

internal class InventoryItemSearchQuery private constructor(internal val terms: List<String>) {
    val hasTerms: Boolean
        get() = terms.isNotEmpty()

    fun matches(stack: ItemStack): Boolean = hasTerms && matchesSearchableText(InventoryItemSearchIndex.text(stack))

    fun matchesSearchableText(text: String): Boolean = hasTerms && terms.all(text::contains)

    companion object {
        val EMPTY = InventoryItemSearchQuery(emptyList())

        fun from(text: String): InventoryItemSearchQuery {
            val terms = text.cleanSkyBlockText()
                .lowercase()
                .trim()
                .split(WHITESPACE_PATTERN)
                .filter(String::isNotEmpty)
            return if (terms.isEmpty()) EMPTY else InventoryItemSearchQuery(terms)
        }

        private val WHITESPACE_PATTERN = Regex("""\s+""")
    }
}

internal object InventoryItemSearchIndex {
    private val itemTextByHash = mutableMapOf<Int, MutableList<IndexedItemText>>()

    fun text(stack: ItemStack): String {
        if (stack.isEmpty) return ""
        val hash = ItemStack.hashItemAndComponents(stack)
        itemTextByHash[hash]?.firstOrNull { ItemStack.isSameItemSameComponents(it.stack, stack) }?.let {
            return it.text
        }
        if (itemTextByHash.size >= MAX_ITEM_HASHES) itemTextByHash.clear()
        return searchableText(stack).also { text ->
            itemTextByHash.getOrPut(hash) { mutableListOf() } += IndexedItemText(stack.copyWithCount(1), text)
        }
    }

    private fun searchableText(stack: ItemStack): String = buildString {
        append(stack.formattedHoverName().cleanSkyBlockText()).append('\n')
        stack.loreLines().forEach { append(it.cleanSkyBlockText()).append('\n') }
    }.lowercase()

    private data class IndexedItemText(val stack: ItemStack, val text: String)

    private const val MAX_ITEM_HASHES = 512
}

internal object InventoryItemSearchHighlight {
    const val OUTLINE_COLOR = 0xFF30FF30.toInt()
    private const val FILL_COLOR = 0x6030FF30
    private const val INSET = 1
    private const val SIZE = 18
    private const val END_OFFSET = SIZE - INSET

    fun render(context: GuiGraphicsExtractor, itemX: Int, itemY: Int) {
        context.fill(
            itemX - INSET,
            itemY - INSET,
            itemX + END_OFFSET,
            itemY + END_OFFSET,
            FILL_COLOR,
        )
    }
}

object ContainerSearchHighlighter {
    private var activeScreen: WeakReference<AbstractContainerScreen<*>>? = null
    private var query = InventoryItemSearchQuery.EMPTY

    @JvmStatic
    fun toggle(screen: AbstractContainerScreen<*>, text: String) {
        if (isActive(screen)) {
            activeScreen = null
            query = InventoryItemSearchQuery.EMPTY
            return
        }
        val nextQuery = InventoryItemSearchQuery.from(text)
        if (!nextQuery.hasTerms) return
        activeScreen = WeakReference(screen)
        query = nextQuery
    }

    @JvmStatic
    fun update(screen: AbstractContainerScreen<*>, text: String) {
        if (!isActive(screen)) return
        query = InventoryItemSearchQuery.from(text)
    }

    @JvmStatic
    fun clear(screen: AbstractContainerScreen<*>) {
        if (!isActive(screen)) return
        activeScreen = null
        query = InventoryItemSearchQuery.EMPTY
    }

    @JvmStatic
    fun isActive(screen: AbstractContainerScreen<*>): Boolean = activeScreen?.get() === screen

    internal fun activeQuery(screen: AbstractContainerScreen<*>): String? =
        query.terms.joinToString(" ").takeIf { isActive(screen) }

    internal fun matches(screen: AbstractContainerScreen<*>, slot: Slot): Boolean =
        isActive(screen) && slot.isActive && query.matches(slot.item)

    @JvmStatic
    fun renderBackground(screen: AbstractContainerScreen<*>, context: GuiGraphicsExtractor, slot: Slot) {
        if (!matches(screen, slot)) return
        InventoryItemSearchHighlight.render(context, slot.x, slot.y)
    }
}
