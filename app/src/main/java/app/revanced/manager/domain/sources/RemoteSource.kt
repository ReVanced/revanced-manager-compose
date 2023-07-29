package app.revanced.manager.domain.sources

import androidx.compose.runtime.Stable
import app.revanced.manager.domain.repository.SourcePersistenceRepository
import app.revanced.manager.network.api.ManagerAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

@Stable
class RemoteSource(name: String, id: Int, directory: File, val apiUrl: String) :
    Source(name, id, directory), KoinComponent {
    private val configRepository: SourcePersistenceRepository by inject()
    private val api: ManagerAPI by inject()

    private suspend fun currentVersion() = configRepository.getProps(uid).first().versionInfo
    private suspend fun saveVersion(patches: String, integrations: String) =
        configRepository.updateVersion(uid, patches, integrations)

    suspend fun downloadLatest() = withContext(Dispatchers.IO) {
        api.downloadBundle(apiUrl, patchesJar, integrations).also { (patchesVer, integrationsVer) ->
            saveVersion(patchesVer, integrationsVer)
            reload()
        }

        return@withContext
    }

    suspend fun update(): Boolean = withContext(Dispatchers.IO) {
        val current = currentVersion().let { it.patches to it.integrations }
        if (!hasInstalled() || current != api.getLatestBundleVersion(apiUrl)) {
            downloadLatest()
            true
        } else false
    }

    suspend fun deleteLocalFiles() = withContext(Dispatchers.IO) {
        arrayOf(patchesJar, integrations).forEach(File::delete)
        reload()
    }

    fun propsFlow() = configRepository.getProps(uid)

    suspend fun setAutoUpdate(value: Boolean) = configRepository.setAutoUpdate(uid, value)
}