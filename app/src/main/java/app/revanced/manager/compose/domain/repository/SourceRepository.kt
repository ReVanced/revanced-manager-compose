package app.revanced.manager.compose.domain.repository

import android.app.Application
import android.util.Log
import app.revanced.manager.compose.data.room.sources.SourceEntity
import app.revanced.manager.compose.data.room.sources.SourceLocation
import app.revanced.manager.compose.domain.sources.RemoteSource
import app.revanced.manager.compose.domain.sources.LocalSource
import app.revanced.manager.compose.domain.sources.Source
import app.revanced.manager.compose.util.tag
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class SourceRepository(app: Application, private val persistenceRepo: SourcePersistenceRepository) {
    private val sourcesDir = app.dataDir.resolve("sources").also { it.mkdirs() }

    private fun directoryOf(uid: Int) = sourcesDir.resolve(uid.toString())

    /**
     * A chainable version of [File.mkdirs].
     */
    private fun File.getOrCreate() = also { mkdirs() }
    private fun SourceEntity.directory() = directoryOf(uid)
    private fun SourceEntity.load(dir: File) = when (location) {
        is SourceLocation.Local -> LocalSource(uid, dir)
        is SourceLocation.Remote -> RemoteSource(uid, dir)
    }

    suspend fun loadSources() = withContext(Dispatchers.Default) {
        val sourcesConfig = persistenceRepo.loadConfiguration().onEach {
            Log.d(tag, "Source: $it")
        }

        val sources = sourcesConfig.associate {
            val dir = it.directory().getOrCreate()
            val source = it.load(dir)

            it.name to source
        }

        _sources.emit(sources)
    }

    suspend fun resetConfig() = withContext(Dispatchers.Default) {
        persistenceRepo.clear()
        _sources.emit(emptyMap())
        sourcesDir.apply {
            delete()
            mkdirs()
        }

        loadSources()
    }

    suspend fun remove(source: Source) = withContext(Dispatchers.Default) {
        persistenceRepo.delete(source.id)
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
        val id = persistenceRepo.create(name, SourceLocation.Local)
        val source = LocalSource(id, directoryOf(id).getOrCreate())

        addSource(name, source)

        source.replace(patches, integrations)
    }

    suspend fun createRemoteSource(name: String, apiUrl: Url) {
        val id = persistenceRepo.create(name, SourceLocation.Remote(apiUrl))
        addSource(name, RemoteSource(id, directoryOf(id).getOrCreate()))
    }

    private val _sources: MutableStateFlow<Map<String, Source>> = MutableStateFlow(emptyMap())
    val sources = _sources.asStateFlow()

    suspend fun redownloadRemoteSources() =
        sources.value.values.filterIsInstance<RemoteSource>().forEach { it.downloadLatest() }
}