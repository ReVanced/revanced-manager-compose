package app.revanced.manager.compose.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Changelog(
    @SerialName("version") val version: String,
    @SerialName("body") val body: String,
    @SerialName("downloadCount") val downloadCount: Int,
    )