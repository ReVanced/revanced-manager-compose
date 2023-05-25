package app.revanced.manager.compose.domain.sources

import app.revanced.manager.compose.network.api.ManagerAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import java.io.File

class RemoteSource(id: Int, directory: File) : Source(id, directory) {
    private val api: ManagerAPI by inject()
    suspend fun downloadLatest() = withContext(Dispatchers.IO) {
        // TODO: use a cache directory for this and then move them to the correct location

        api.downloadBundle(patchesJar, integrations).also { (patchesVer, integrationsVer) ->
            saveVersion(patchesVer, integrationsVer)

            withContext(Dispatchers.Main) {
                _bundle.emit(loadBundle { err -> throw err })
            }
        }

        return@withContext
    }

    suspend fun update() = withContext(Dispatchers.IO) {
        val currentVersion = getVersion()
        if (!hasDownloaded() || currentVersion != api.getLatestBundleVersion()) {
            downloadLatest()
        }
    }
}