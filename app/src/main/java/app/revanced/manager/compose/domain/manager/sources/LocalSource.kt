package app.revanced.manager.compose.domain.manager.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files

class LocalSource(directory: File) : Source(directory) {
    override val mutableBundle = MutableStateFlow(loadBundle())

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
            mutableBundle.emit(loadBundle { throw it })
        }
    }
}
