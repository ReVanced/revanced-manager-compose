package app.revanced.manager.compose.domain.manager.sources

import android.util.Log
import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * A [PatchBundle] source.
 */
abstract class Source(directory: File) {
    abstract val bundle: StateFlow<PatchBundle>

    companion object {
        /**
         * A placeholder [PatchBundle].
         */
        val emptyPatchBundle = PatchBundle(emptyList(), null)
        fun onFailDefault(err: Throwable) {
            Log.e("revanced-manager-custom-sources", "Failed to load bundle", err)
        }
    }

    protected val patchesJar = directory.resolve("patches.jar")
    protected val integrations = directory.resolve("integrations.apk")
}

