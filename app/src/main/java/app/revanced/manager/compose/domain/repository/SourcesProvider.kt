package app.revanced.manager.compose.domain.repository

import android.app.Application
import app.revanced.manager.compose.domain.manager.sources.RemoteSource
import app.revanced.manager.compose.domain.manager.sources.DebugSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SourcesProvider(app: Application) {
    private val sourcesDir = app.dataDir.resolve("sources").also { it.mkdirs() }

    // TODO: make this configurable by the user.
    private fun loadConfig() = mapOf(
        "Official" to RemoteSource(sourcesDir.resolve("Official").also { it.mkdirs() }),
        "Testing" to DebugSource()
    )

    private val _sources = MutableStateFlow(loadConfig())
    val sources = _sources.asStateFlow()

    suspend fun reloadSources() =
        sources.value.values.filterIsInstance<RemoteSource>().forEach { it.downloadLatest() }
}