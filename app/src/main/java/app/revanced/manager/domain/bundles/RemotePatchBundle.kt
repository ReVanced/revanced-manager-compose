package app.revanced.manager.domain.bundles

import androidx.compose.runtime.Stable
import app.revanced.manager.data.room.bundles.VersionInfo
import app.revanced.manager.domain.repository.Assets
import app.revanced.manager.domain.repository.PatchBundlePersistenceRepository
import app.revanced.manager.domain.repository.ReVancedRepository
import app.revanced.manager.network.dto.BundleInfo
import app.revanced.manager.network.service.HttpService
import app.revanced.manager.network.utils.getOrThrow
import app.revanced.manager.util.ghIntegrations
import app.revanced.manager.util.ghPatches
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

@Stable
sealed class RemotePatchBundle<Meta>(name: String, id: Int, directory: File, val endpoint: String) :
    PatchBundleSource(name, id, directory), KoinComponent {
    private val configRepository: PatchBundlePersistenceRepository by inject()
    protected val http: HttpService by inject()

    protected abstract suspend fun download(metadata: Meta)
    protected abstract suspend fun getLatestMetadata(): Meta
    protected abstract fun getVersionInfo(metadata: Meta): VersionInfo

    suspend fun downloadLatest() {
        download(getLatestMetadata())
    }

    protected suspend fun downloadAssets(assets: Map<String, File>) = coroutineScope {
        assets.forEach { (asset, file) ->
            launch {
                http.download(file) {
                    url(asset)
                }
            }
        }
    }

    suspend fun update(): Boolean = withContext(Dispatchers.IO) {
        val metadata = getLatestMetadata()
        if (hasInstalled() && getVersionInfo(metadata) == currentVersion()) {
            return@withContext false
        }

        download(metadata)
        true
    }

    private suspend fun currentVersion() = configRepository.getProps(uid).first().versionInfo
    protected suspend fun saveVersion(patches: String, integrations: String) =
        configRepository.updateVersion(uid, patches, integrations)

    suspend fun deleteLocalFiles() = withContext(Dispatchers.Default) {
        arrayOf(patchesFile, integrationsFile).forEach(File::delete)
        reload()
    }

    fun propsFlow() = configRepository.getProps(uid)

    suspend fun setAutoUpdate(value: Boolean) = configRepository.setAutoUpdate(uid, value)

    companion object {
        const val updateFailMsg = "Failed to update patch bundle(s)"
    }
}

class JsonPatchBundle(name: String, id: Int, directory: File, endpoint: String) :
    RemotePatchBundle<BundleInfo>(name, id, directory, endpoint) {
    override suspend fun getLatestMetadata() = withContext(Dispatchers.IO) {
        http.request<BundleInfo> {
            url(endpoint)
        }.getOrThrow()
    }

    override fun getVersionInfo(metadata: BundleInfo) =
        VersionInfo(metadata.patches.version, metadata.integrations.version)

    override suspend fun download(metadata: BundleInfo) = withContext(Dispatchers.IO) {
        val (patches, integrations) = metadata
        downloadAssets(
            mapOf(
                patches.url to patchesFile,
                integrations.url to integrationsFile
            )
        )

        saveVersion(patches.version, integrations.version)
        reload()
    }
}

class APIPatchBundle(name: String, id: Int, directory: File, endpoint: String) :
    RemotePatchBundle<Assets>(name, id, directory, endpoint) {
    private val api: ReVancedRepository by inject()

    override suspend fun getLatestMetadata() = api.getAssets()
    override fun getVersionInfo(metadata: Assets) = metadata.let { (patches, integrations) ->
        VersionInfo(
            patches.version,
            integrations.version
        )
    }

    override suspend fun download(metadata: Assets) = withContext(Dispatchers.IO) {
        val (patches, integrations) = metadata
        downloadAssets(
            mapOf(
                patches.downloadUrl to patchesFile,
                integrations.downloadUrl to integrationsFile
            )
        )

        saveVersion(patches.version, integrations.version)
        reload()
    }

    private companion object {
        operator fun Assets.component1() = find(ghPatches, ".jar")
        operator fun Assets.component2() = find(ghIntegrations, ".apk")
    }
}