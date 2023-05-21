package app.revanced.manager.compose.domain.repository

import android.app.Application
import app.revanced.manager.compose.domain.manager.sources.Source
import app.revanced.manager.compose.domain.manager.sources.impl.NetworkSource
import app.revanced.manager.compose.domain.manager.sources.impl.ReVancedAPISource
import app.revanced.manager.compose.network.api.ManagerAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class SourcesProvider(app: Application, private val managerAPI: ManagerAPI) {
    private val sourcesDir = app.dataDir.resolve("sources").also { it.mkdirs() }

    private fun loadConfig() = mapOf<String, Source>("official" to ReVancedAPISource(sourcesDir.resolve("manager").also { it.mkdirs() }, managerAPI))

    // TODO: make this configurable and reloadable.
    private val _sources = MutableStateFlow(loadConfig())
    val sources = _sources.asStateFlow()

    suspend fun updateSources() =
        sources.first().values.forEach { if (it is NetworkSource) it.downloadLatest() }
}