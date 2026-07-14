package com.skysoft.features.misc.update

import com.google.gson.annotations.SerializedName

data class ModrinthVersionInfo(
    val id: String,
    @SerializedName("version_number")
    val versionNumber: String,
    @SerializedName("version_type")
    val versionType: String,
)

data class SkysoftUpdate(
    val version: String,
    val url: String,
)

enum class DownloadOpenResult {
    OPENED,
    NOT_READY,
    FAILED,
}
