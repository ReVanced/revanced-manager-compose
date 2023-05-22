package app.revanced.manager.compose.domain.manager.sources.impl

import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import app.revanced.manager.compose.domain.manager.sources.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files

class LocalSource(directory: File) : Source(directory) {
    override val bundle = MutableStateFlow(loadBundle())

    private fun loadBundle(onFail: (Throwable) -> Unit = ::logError) = try {
        PatchBundle(patchesJar, integrations)
    } catch (err: Throwable) {
        onFail(err)
        emptyPatchBundle
    }

    suspend fun replace(patches: File? = null, integrations: File? = null) {
        withContext(Dispatchers.IO) {
            patches?.let {
                Files.copy(it.toPath(), patchesJar.toPath())
            }
            integrations?.let {
                Files.copy(it.toPath(), this@LocalSource.integrations.toPath())
            }
        }

        withContext(Dispatchers.Main) {
            bundle.emit(loadBundle { throw it })
        }
    }
}
