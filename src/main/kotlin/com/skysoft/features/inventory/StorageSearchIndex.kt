package com.skysoft.features.inventory

import com.skysoft.data.ProfileStorage
import com.skysoft.data.skyblock.SkyBlockItemUtilities.formattedHoverName
import com.skysoft.data.skyblock.SkyBlockItemUtilities.loreLines
import com.skysoft.utils.TextUtilities.cleanSkyBlockText
import java.util.IdentityHashMap
import net.minecraft.world.item.ItemStack

internal object StorageSearchIndex {
    private var indexedQuery = ""
    private var indexedTerms: List<String> = emptyList()
    private val itemTextByEncodedStack = mutableMapOf<String, String>()
    private val liveItemTextByHash = mutableMapOf<Int, MutableList<LiveItemText>>()
    private val pageMatches = IdentityHashMap<ProfileStorage.SkyBlockStoragePageData, Boolean>()

    val hasQuery: Boolean
        get() = terms().isNotEmpty()

    fun matches(page: ProfileStorage.SkyBlockStoragePageData): Boolean {
        val queryTerms = terms()
        if (queryTerms.isEmpty()) return true
        return pageMatches.getOrPut(page) {
            page.items.any { item -> matches(item, queryTerms) }
        }
    }

    fun matches(item: ProfileStorage.SkyBlockStorageItemData?): Boolean {
        val queryTerms = terms()
        return queryTerms.isEmpty() || matches(item, queryTerms)
    }

    fun matches(stack: ItemStack): Boolean {
        val queryTerms = terms()
        return queryTerms.isEmpty() || matches(liveSearchableText(stack), queryTerms)
    }

    fun invalidatePages() {
        pageMatches.clear()
    }

    fun clear() {
        indexedQuery = ""
        indexedTerms = emptyList()
        itemTextByEncodedStack.clear()
        liveItemTextByHash.clear()
        pageMatches.clear()
    }

    private fun terms(): List<String> {
        val query = searchText.cleanSkyBlockText().lowercase().trim()
        if (query == indexedQuery) return indexedTerms
        indexedQuery = query
        indexedTerms = query.split(whitespacePattern).filter { it.isNotEmpty() }
        pageMatches.clear()
        return indexedTerms
    }

    private fun matches(item: ProfileStorage.SkyBlockStorageItemData?, queryTerms: List<String>): Boolean {
        val encoded = item?.encodedStack?.takeIf { it.isNotBlank() } ?: return false
        val searchableText = itemTextByEncodedStack.getOrPut(encoded) { searchableText(stackFor(item)) }
        return matches(searchableText, queryTerms)
    }

    private fun matches(searchableText: String, queryTerms: List<String>): Boolean =
        queryTerms.all(searchableText::contains)

    private fun liveSearchableText(stack: ItemStack): String {
        if (stack.isEmpty) return ""
        val hash = ItemStack.hashItemAndComponents(stack)
        liveItemTextByHash[hash]?.firstOrNull { ItemStack.isSameItemSameComponents(it.stack, stack) }?.let {
            return it.text
        }
        if (liveItemTextByHash.size >= MAX_LIVE_ITEM_HASHES) liveItemTextByHash.clear()
        return searchableText(stack).also { text ->
            liveItemTextByHash.getOrPut(hash) { mutableListOf() } += LiveItemText(stack.copyWithCount(1), text)
        }
    }

    private fun searchableText(stack: ItemStack): String {
        if (stack.isEmpty) return ""
        return buildString {
            append(stack.formattedHoverName().cleanSkyBlockText()).append('\n')
            stack.loreLines().forEach { append(it.cleanSkyBlockText()).append('\n') }
        }.lowercase()
    }

    private val whitespacePattern = Regex("""\s+""")

    private data class LiveItemText(val stack: ItemStack, val text: String)

    private const val MAX_LIVE_ITEM_HASHES = 512
}
