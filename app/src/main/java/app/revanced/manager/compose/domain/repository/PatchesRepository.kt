package app.revanced.manager.compose.domain.repository

import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class PatchesRepository(private val sourcesProvider: SourcesProvider) {
    private data class UpdatedSource(val name: String, val bundle: PatchBundle)

    /**
     * A [Flow] that emits whenever the sources change.
     *
     * The outer flow emits whenever the sources configuration changes.
     * The inner flow emits whenever one of the bundles update.
     */
    private val sourceUpdates = sourcesProvider.sources.map {
        merge(*it.map { (key, value) ->
            value.bundle.map { bundle ->
                UpdatedSource(
                    key,
                    bundle
                )
            }
        }.toTypedArray())
    }

    // TODO: use Flow.stateIn(mainScope) to avoid reloading all the bundles everytime someone collects the flow.
    @OptIn(FlowPreview::class)
    val bundles: Flow<Map<String, PatchBundle>> = sourceUpdates.map { updatesFlow ->
        MutableStateFlow(mapOf<String, PatchBundle>()).zip(updatesFlow) { bundleMap, updated ->
            bundleMap.toMutableMap().also { it[updated.name] = updated.bundle }
        }
    }.flattenConcat()

    suspend fun loadPatchClassesFiltered(packageName: String) =
        bundles.first()["official"]!!.loadPatchesFiltered(packageName)

    suspend fun getIntegrations() = bundles.first().values.mapNotNull { it.integrations }

}