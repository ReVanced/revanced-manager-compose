package app.revanced.manager.compose.domain.manager.sources.impl

import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import app.revanced.manager.compose.domain.manager.sources.Source
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class LocalSource(directory: File) : Source(directory) {
    override val bundle = MutableStateFlow(loadBundle())

    private fun loadBundle(onFail: (Throwable) -> Unit = ::onFailDefault) = try {
        PatchBundle(patchesJar.absolutePath, integrations)
    } catch (err: Throwable) {
        onFail(err)
        emptyPatchBundle
    }

    // TODO: write function that allows the user to replace the files
}
