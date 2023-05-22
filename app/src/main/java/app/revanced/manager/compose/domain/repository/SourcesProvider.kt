package app.revanced.manager.compose.domain.repository

import android.app.Application
import app.revanced.manager.compose.domain.manager.sources.Source
import app.revanced.manager.compose.domain.manager.sources.NetworkSource
import app.revanced.manager.compose.domain.manager.sources.impl.DebugSource
import app.revanced.manager.compose.domain.manager.sources.impl.ReVancedAPISource
import app.revanced.manager.compose.network.api.ManagerAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.io.File

class SourcesProvider(app: Application, private val managerAPI: ManagerAPI) {
    private val sourcesDir = app.dataDir.resolve("sources").also { it.mkdirs() }

    // TODO: make this configurable by the user.
    private fun loadConfig() = mapOf(
        "Official" to ReVancedAPISource(sourcesDir.resolve("manager").also { it.mkdirs() }, managerAPI),
        "Testing" to DebugSource()
    )

    private val _sources = MutableStateFlow(loadConfig())
    val sources = _sources.asStateFlow()

    suspend fun updateSources() =
        sources.first().values.forEach { if (it is NetworkSource) it.downloadLatest() }
}