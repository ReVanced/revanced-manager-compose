package app.revanced.manager.compose.network.api

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.revanced.manager.compose.domain.repository.ReVancedRepository
import app.revanced.manager.compose.util.ghIntegrations
import app.revanced.manager.compose.util.ghPatches
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

class ManagerAPI(
    private val client: HttpClient,
    private val revancedRepository: ReVancedRepository
) {
    var downloadProgress: Float? by mutableStateOf(null)

    private suspend fun downloadAsset(downloadUrl: String, saveLocation: File) {
        client.get(downloadUrl) {
            onDownload { bytesSentTotal, contentLength ->
                downloadProgress = (bytesSentTotal.toFloat() / contentLength.toFloat())
            }
        }.bodyAsChannel().copyAndClose(saveLocation.writeChannel())
        downloadProgress = null
    }

    private suspend fun patchesAsset() = revancedRepository.findAsset(ghPatches, ".jar")
    private suspend fun integrationsAsset() = revancedRepository.findAsset(ghIntegrations, ".apk")

    suspend fun getLatestVersion() = patchesAsset().version to integrationsAsset().version

    suspend fun download(patchBundle: File, integrations: File): Pair<String, String> {
        val patchBundleAsset = patchesAsset()
        val integrationsAsset = integrationsAsset()

        downloadAsset(patchBundleAsset.downloadUrl, patchBundle)
        downloadAsset(integrationsAsset.downloadUrl, integrations)

        return patchBundleAsset.version to integrationsAsset.version
    }
}

data class PatchesAsset(
    val downloadUrl: String, val name: String, val version: String
)

class MissingAssetException : Exception()