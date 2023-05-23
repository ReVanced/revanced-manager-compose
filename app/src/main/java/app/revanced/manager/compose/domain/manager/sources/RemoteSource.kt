package app.revanced.manager.compose.domain.manager.sources

import app.revanced.manager.compose.network.api.ManagerAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class RemoteSource(directory: File) : Source(directory), KoinComponent {
    private val api: ManagerAPI by inject()

    override val mutableBundle = MutableStateFlow(loadBundle())

    fun hasDownloaded() = patchesJar.exists() && integrations.exists()

    override fun loadBundle(
        onFail: (Throwable) -> Unit
    ) = if (!hasDownloaded()) emptyPatchBundle else super.loadBundle(onFail)

    suspend fun downloadLatest() = withContext(Dispatchers.IO) {
        // TODO: use a cache directory for this and then move them to the correct location

        api.downloadBundle(patchesJar, integrations).also { (patchesVer, integrationsVer) ->
            saveVersion(patchesVer, integrationsVer)

            withContext(Dispatchers.Main) {
                mutableBundle.emit(loadBundle { err -> throw err })
            }
        }

        return@withContext
    }

    suspend fun update() = withContext(Dispatchers.IO) {
        val currentVersion = "0.0.0" to "0.0.0"
        if (!hasDownloaded() || currentVersion != api.getLatestBundleVersion()) {
            downloadLatest()
        }
    }
}