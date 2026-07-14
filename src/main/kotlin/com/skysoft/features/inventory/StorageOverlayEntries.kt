package com.skysoft.features.inventory

import com.skysoft.data.ProfileStorage

internal fun storageEntry(pageIndex: Int): ProfileStorage.SkyBlockStoragePageData? =
    ToolkitType.fromPageIndex(pageIndex)?.let { storage.skyBlockToolkits[it.storageKey] }
        ?: storage.skyBlockStoragePages[pageIndex]

internal fun storageEntryExists(pageIndex: Int): Boolean =
    ToolkitType.fromPageIndex(pageIndex)?.let { it.storageKey in storage.skyBlockToolkits }
        ?: (pageIndex in storage.skyBlockStoragePages)

internal fun displayStorageEntries(): List<Pair<Int, ProfileStorage.SkyBlockStoragePageData>> = buildList {
    storage.skyBlockStoragePages.toSortedMap().forEach { (pageIndex, page) ->
        if (pageIndex in 0 until ProfileStorage.SKYBLOCK_STORAGE_PAGE_COUNT) add(pageIndex to page)
    }
    ToolkitType.entries.forEach { type ->
        storage.skyBlockToolkits[type.storageKey]?.let { add(type.pageIndex to it) }
    }
}

internal fun StorageHandle.entryIndex(): Int? = when (this) {
    StorageHandle.Overview -> null
    is StorageHandle.Page -> pageIndex
    is StorageHandle.Toolkit -> type.pageIndex
}

internal fun StorageHandle.gridRows(): Int? = when (this) {
    StorageHandle.Overview -> null
    is StorageHandle.Page -> rows
    is StorageHandle.Toolkit -> rows
}

internal fun StorageHandle.slotOffset(): Int = when (this) {
    StorageHandle.Overview -> 0
    is StorageHandle.Page -> StoragePages.COLUMNS
    is StorageHandle.Toolkit -> 0
}
