package app.revanced.manager.compose.domain.manager.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Files

class LocalSource(id: Int, directory: File) : Source(id, directory) {
    suspend fun replace(patches: InputStream? = null, integrations: InputStream? = null) {
        withContext(Dispatchers.IO) {
            patches?.let {
                Files.copy(it, patchesJar.toPath())
            }
            integrations?.let {
                Files.copy(it, this@LocalSource.integrations.toPath())
            }
        }

        withContext(Dispatchers.Main) {
            _bundle.emit(loadBundle { throw it })
        }
    }
}
