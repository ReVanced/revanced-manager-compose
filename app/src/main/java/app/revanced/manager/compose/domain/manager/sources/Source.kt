package app.revanced.manager.compose.domain.manager.sources

import android.util.Log
import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * A [PatchBundle] source.
 */
sealed class Source(directory: File) {
    protected abstract val mutableBundle: MutableStateFlow<PatchBundle>
    val bundle get() = mutableBundle.asStateFlow()

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

    protected suspend fun saveVersion(patches: String, integrations: String) {
        // TODO: actually save it somewhere.
    }

    protected open fun loadBundle(onFail: (Throwable) -> Unit = ::logError) = try {
        PatchBundle(patchesJar, integrations)
    } catch (err: Throwable) {
        onFail(err)
        emptyPatchBundle
    }
}