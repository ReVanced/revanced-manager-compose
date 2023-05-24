package app.revanced.manager.compose.domain.repository

import android.app.Application
import android.util.Log
import app.revanced.manager.compose.data.room.sources.SourceEntity
import app.revanced.manager.compose.data.room.sources.SourceLocation
import app.revanced.manager.compose.domain.manager.sources.RemoteSource
import app.revanced.manager.compose.domain.manager.sources.LocalSource
import app.revanced.manager.compose.domain.manager.sources.Source
import app.revanced.manager.compose.util.tag
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class SourcesProvider(app: Application, private val configRepository: SourceConfigRepository) {
    private val sourcesDir = app.dataDir.resolve("sources").also { it.mkdirs() }

    private fun directoryOf(uid: Int) = sourcesDir.resolve(uid.toString())

    /**
     * Create the directory if it does not exist.
     */
    private fun File.create() = also { mkdirs() }
    private fun SourceEntity.directory() = directoryOf(uid)
    private fun SourceEntity.load(dir: File) = when (location) {
        is SourceLocation.Local -> LocalSource(uid, dir)
        is SourceLocation.Remote -> RemoteSource(uid, dir)
    }

    suspend fun loadSources() = withContext(Dispatchers.Default) {
        val sourcesConfig = configRepository.loadConfiguration().onEach {
            Log.d(tag, "Source: $it")
        }

        val sources = sourcesConfig.associate {
            val dir = it.directory().create()
            val source = it.load(dir)

            it.name to source
        }

        _sources.emit(sources)
    }

    suspend fun resetConfig() = withContext(Dispatchers.Default) {
        configRepository.clear()
        _sources.emit(emptyMap())
        sourcesDir.apply {
            delete()
            mkdirs()
        }

        loadSources()
    }

    suspend fun remove(source: Source) = withContext(Dispatchers.Default) {
        configRepository.delete(source.id)
        sourcesDir.resolve(source.id.toString()).delete()

        _sources.update {
            it.filterValues { value ->
                value.id != source.id
            }
        }
    }

    private fun addSource(name: String, source: Source) =
        _sources.update { it.toMutableMap().apply { put(name, source) } }

    suspend fun createLocalSource(name: String, patches: InputStream, integrations: InputStream?) {
        val id = configRepository.create(name, SourceLocation.Local)
        val source = LocalSource(id, directoryOf(id).create())

        addSource(name, source)

        source.replace(patches, integrations)
    }

    suspend fun createRemoteSource(name: String, apiUrl: Url) {
        val id = configRepository.create(name, SourceLocation.Remote(apiUrl))
        addSource(name, RemoteSource(id, directoryOf(id).create()))
    }

    private val _sources: MutableStateFlow<Map<String, Source>> = MutableStateFlow(emptyMap())
    val sources = _sources.asStateFlow()

    suspend fun redownloadRemoteSources() =
        sources.value.values.filterIsInstance<RemoteSource>().forEach { it.downloadLatest() }
}