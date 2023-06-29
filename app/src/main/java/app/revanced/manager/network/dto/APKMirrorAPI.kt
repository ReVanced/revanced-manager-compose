package app.revanced.manager.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class APKMirrorResponse(
    @SerialName("data") val data: List<Data>
)

@Serializable
data class Data(
    @SerialName("exists") val exists: Boolean,
    @SerialName("app") val app: App? = null
)

@Serializable
data class App(
    @SerialName("link") val link: String
)