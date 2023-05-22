package app.revanced.manager.compose.domain.manager.sources

import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

abstract class NetworkSource(directory: File) : Source(directory) {
    protected data class BundleVersion(val patches: String, val integrations: String)

    protected abstract suspend fun downloadLatest(patches: File, integrations: File): BundleVersion

    protected abstract suspend fun getLatestVersion(): BundleVersion

    fun hasDownloaded() = patchesJar.exists() && integrations.exists()

    private fun loadBundle(
        onFail: (Throwable) -> Unit = ::onFailDefault
    ): PatchBundle {
        var bundle: PatchBundle = emptyPatchBundle
        if (hasDownloaded()) {
            try {
                bundle = PatchBundle(patchesJar, integrations)
            } catch (e: Throwable) {
                onFail(e)
            }
        }
        return bundle
    }

    private val mutableBundle = MutableStateFlow(loadBundle())
    override val bundle: StateFlow<PatchBundle> = mutableBundle.asStateFlow()

    private suspend fun saveVersion(version: BundleVersion) {
        // TODO: actually save it somewhere.
    }

    suspend fun downloadLatest() = withContext(Dispatchers.IO) {
        // TODO: use a cache directory for this and then move them to the correct location

        downloadLatest(patchesJar, integrations).also {
            saveVersion(it)

            withContext(Dispatchers.Main) {
                mutableBundle.emit(loadBundle { err -> throw err })
            }
        }

        return@withContext
    }

    suspend fun update() = withContext(Dispatchers.IO) {
        val currentVersion = BundleVersion("0.0.0", "0.0.0")
        if (!hasDownloaded() || currentVersion != getLatestVersion()) {
            downloadLatest()
        }
    }
}