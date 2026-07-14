package com.skysoft.features.pets

import com.skysoft.data.StoredPetData

internal fun MutableList<StoredPetData>.addOrReplace(petData: StoredPetData, matches: (StoredPetData) -> Boolean) {
    val index = indexOfFirst(matches)
    if (index >= 0) this[index] = petData else add(petData)
}
