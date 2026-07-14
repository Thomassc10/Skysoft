package com.skysoft.features.pets

object CanonicalItemNames {
    fun resolve(internalName: String): String? {
        PetRepoCache.itemNames[internalName]?.let { return it }
        LocalSkyBlockRepo.itemNameOrNull(internalName)?.let { itemName ->
            PetRepoCache.itemNames[internalName] = itemName
            return itemName
        }
        RemoteSkyBlockRepo.requestItem(internalName)
        return null
    }
}
