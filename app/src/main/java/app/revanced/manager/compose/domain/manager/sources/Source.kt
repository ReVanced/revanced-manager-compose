package app.revanced.manager.compose.domain.manager.sources

import android.util.Log
import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import app.revanced.manager.compose.domain.repository.SourceConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * A [PatchBundle] source.
 */
sealed class Source(val id: Int, directory: File) : KoinComponent {
    private val configRepository: SourceConfigRepository by inject()
    protected companion object {
        /**
         * A placeholder [PatchBundle].
         */
        val emptyPatchBundle = PatchBundle(emptyList(), null)
        fun logError(err: Throwable) {
            Log.e("revanced-manager-custom-sources", "Failed to load bundle", err)
        }
    }

    protected val patchesJar = directory.resolve("patches.jar")
    protected val integrations = directory.resolve("integrations.apk")

    // TODO: this really needs a better name
    fun hasDownloaded() = patchesJar.exists()

    protected suspend fun getVersion() = configRepository.getVersion(id)
    protected suspend fun saveVersion(patches: String, integrations: String) =
        configRepository.updateVersion(id, patches, integrations)

    protected fun loadBundle(onFail: (Throwable) -> Unit = ::logError) = if (!hasDownloaded()) emptyPatchBundle
    else try {
        PatchBundle(patchesJar, integrations.takeIf { it.exists() })
    } catch (err: Throwable) {
        onFail(err)
        emptyPatchBundle
    }

    protected val _bundle = MutableStateFlow(loadBundle())
    val bundle = _bundle.asStateFlow()
}