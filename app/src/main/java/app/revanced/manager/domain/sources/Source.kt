package app.revanced.manager.domain.sources

import androidx.compose.runtime.Stable
import app.revanced.manager.patcher.patch.PatchBundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * A [PatchBundle] source.
 */
@Stable
sealed class Source(val name: String, val uid: Int, directory: File) {
    protected companion object {
        /**
         * A placeholder [PatchBundle].
         */
        val emptyPatchBundle = PatchBundle(emptyList(), null)
    }

    protected val patchesJar = directory.resolve("patches.jar")
    protected val integrations = directory.resolve("integrations.apk")

    /**
     * Returns true if the bundle has been downloaded to local storage.
     *
     * TODO: delete this?
     */
    fun hasInstalled() = patchesJar.exists()

    protected fun load(): State {
        if (!hasInstalled()) return State.Missing

        return try {
            State.Loaded(PatchBundle(patchesJar, integrations.takeIf(File::exists)))
        } catch (t: Throwable) {
            State.Failed(t)
        }
    }

    // TODO: consider renaming
    protected val _bundle = MutableStateFlow(load())
    val bundle = _bundle.asStateFlow()

    sealed interface State {
        fun bundleOrNull(): PatchBundle? = null

        object Missing : State
        data class Failed(val throwable: Throwable) : State
        data class Loaded(val bundle: PatchBundle) : State {
            override fun bundleOrNull() = bundle
        }
    }
}