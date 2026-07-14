package com.skysoft.features.bazaar

import com.skysoft.data.hypixel.CookieBuffState
import com.skysoft.data.hypixel.CookieBuffStatus

data class BazaarRemoteAccess(
    val canOpen: Boolean,
    val unavailableReason: String? = null,
)

fun bazaarRemoteAccess(isInSkyBlock: Boolean, cookieBuff: CookieBuffStatus): BazaarRemoteAccess = when {
    !isInSkyBlock -> BazaarRemoteAccess(false, "Available on Hypixel SkyBlock")
    cookieBuff.state == CookieBuffState.INACTIVE -> BazaarRemoteAccess(false, "Requires an active Cookie Buff")
    cookieBuff.state == CookieBuffState.LOADING -> BazaarRemoteAccess(false, "Cookie Buff status is still loading")
    cookieBuff.state == CookieBuffState.UNKNOWN -> BazaarRemoteAccess(false, "Cookie Buff status is unavailable")
    else -> BazaarRemoteAccess(true)
}
