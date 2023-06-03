package app.revanced.manager.compose.domain.sources

import app.revanced.manager.compose.network.api.ManagerAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.get
import java.io.File

class RemoteSource(id: Int, directory: File) : Source(id, directory) {
    private val api: ManagerAPI = get()
    suspend fun downloadLatest() = withContext(Dispatchers.IO) {
        api.downloadBundle(patchesJar, integrations).also { (patchesVer, integrationsVer) ->
            saveVersion(patchesVer, integrationsVer)
            _bundle.emit(loadBundle { err -> throw err })
        }

        return@withContext
    }

    suspend fun update() = withContext(Dispatchers.IO) {
        val currentVersion = getVersion()
        if (!hasInstalled() || currentVersion != api.getLatestBundleVersion()) {
            downloadLatest()
        }
    }
}