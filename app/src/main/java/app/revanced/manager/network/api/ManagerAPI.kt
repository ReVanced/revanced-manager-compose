package app.revanced.manager.network.api

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.revanced.manager.domain.repository.Assets
import app.revanced.manager.domain.repository.ReVancedRepository
import app.revanced.manager.network.dto.Asset
import app.revanced.manager.network.service.HttpService
import app.revanced.manager.util.*
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.url
import java.io.File

// TODO: merge ReVancedRepository into this class
class ManagerAPI(
    private val http: HttpService,
    private val revancedRepository: ReVancedRepository
) {
    var downloadProgress: Float? by mutableStateOf(null)
    var downloadedSize: Long? by mutableStateOf(null)
    var totalSize: Long? by mutableStateOf(null)

    private suspend fun downloadAsset(asset: Asset, saveLocation: File) {
        http.download(saveLocation) {
            url(asset.downloadUrl)
            onDownload { bytesSentTotal, contentLength ->
                downloadProgress = (bytesSentTotal.toFloat() / contentLength.toFloat())
                downloadedSize = bytesSentTotal
                totalSize = contentLength
            }
        }
        downloadProgress = null
    }

    suspend fun getLatestBundleVersion(api: String) = revancedRepository.getAssets(api).let { (patches, integrations) ->
        patches.version to integrations.version
    }

    suspend fun downloadBundle(api: String, patchBundle: File, integrations: File): Pair<String, String> {
        val (patchBundleAsset, integrationsAsset) = revancedRepository.getAssets(api)

        downloadAsset(patchBundleAsset, patchBundle)
        downloadAsset(integrationsAsset, integrations)

        return patchBundleAsset.version to integrationsAsset.version
    }

    suspend fun downloadManager(location: File) {
        val managerAsset = revancedRepository.getAssets().find(ghManager, ".apk")
        downloadAsset(managerAsset, location)
    }

    private companion object {
        operator fun Assets.component1() = find(ghPatches, ".jar")
        operator fun Assets.component2() = find(ghIntegrations, ".apk")
    }
}

class MissingAssetException : Exception()