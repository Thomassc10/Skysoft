package com.skysoft.features.inventory.itemlist

import com.skysoft.config.ItemListConfig
import com.skysoft.config.SkysoftConfigGui
import com.skysoft.data.skyblock.ItemListEntryKey
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

internal object ItemListState {
    var search = ""
    var page = 0
    var isTemporarilyHidden = false
    var conflictNoticeShown = false

    private val config: ItemListConfig get() = SkysoftConfigGui.config().inventory.itemList

    fun register() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            search = ""
            page = 0
            isTemporarilyHidden = false
            conflictNoticeShown = false
        }
    }

    fun isFavorite(key: ItemListEntryKey): Boolean = key.serialized() in config.favorites

    fun toggleFavorite(key: ItemListEntryKey) {
        val serialized = key.serialized()
        if (!config.favorites.remove(serialized)) {
            config.favorites.add(0, serialized)
            while (config.favorites.size > ItemListConfig.MAX_FAVORITES) config.favorites.removeLast()
        }
        SkysoftConfigGui.config().saveNow()
    }

    fun favorites(): List<ItemListEntryKey> = config.favorites.mapNotNull(ItemListEntryKey::parse)

    fun recordRecent(key: ItemListEntryKey) {
        val serialized = key.serialized()
        config.recentItems.remove(serialized)
        config.recentItems.add(0, serialized)
        while (config.recentItems.size > ItemListConfig.MAX_RECENT_ITEMS) config.recentItems.removeLast()
        SkysoftConfigGui.config().saveNow()
    }
}
