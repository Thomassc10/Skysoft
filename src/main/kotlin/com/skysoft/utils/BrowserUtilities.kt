package com.skysoft.utils

import com.skysoft.SkysoftMod
import net.minecraft.util.Util

internal object BrowserUtilities {
    fun open(url: String): OpenResult =
        try {
            Util.getPlatform().openUri(url)
            OpenResult.OPENED
        } catch (e: Exception) {
            SkysoftMod.LOGGER.warn("Failed to open browser for {}", url, e)
            OpenResult.FAILED
        }

    enum class OpenResult {
        OPENED,
        FAILED,
    }
}
