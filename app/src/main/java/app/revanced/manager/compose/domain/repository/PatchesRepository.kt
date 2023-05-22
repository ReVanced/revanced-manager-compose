package app.revanced.manager.compose.domain.repository

import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger

class PatchesRepository(private val sourcesProvider: SourcesProvider) {
    /**
     * @param name The name of the source that changed.
     * @param bundle The new [PatchBundle].
     * @param counter Incremented once for each event. Used by the [bundles] flow to track state.
     */
    private data class SourceUpdateEvent(val name: String, val bundle: PatchBundle, val counter: Number)

    /**
     * @param bundles A [Map] of the current bundles.
     * @param currentCounter The [SourceUpdateEvent.counter] of the most recent [SourceUpdateEvent].
     */
    private data class BundlesFlowState(val bundles: Map<String, PatchBundle>, val currentCounter: Number)

    /**
     * A [Flow] that emits whenever the sources change.
     *
     * The outer flow emits whenever the sources configuration changes.
     * The inner flow emits whenever one of the bundles update.
     */
    private val sourceUpdates = sourcesProvider.sources.map { sources ->
        val changeCounter = AtomicInteger(0) // Use atomics so threads can't mess anything up.

        sources.map { (name, source) ->
            source.bundle.map { bundle ->
                SourceUpdateEvent(
                    name,
                    bundle,
                    changeCounter.getAndIncrement()
                )
            }
        }.merge().buffer()
    }

    @OptIn(FlowPreview::class)
    val bundles: Flow<Map<String, PatchBundle>> = sourceUpdates.map { updatesFlow ->
        val stateFlow = MutableStateFlow(BundlesFlowState(emptyMap(), -1))

        stateFlow.combineTransform(updatesFlow) { flowState, updateEvent ->
            if (flowState.currentCounter != updateEvent.counter) {
                // Update the state if one of the bundles updated.
                // This will rerun the combineTransform callback.
                stateFlow.emit(BundlesFlowState(flowState.bundles.toMutableMap().also {
                    it[updateEvent.name] = updateEvent.bundle
                }, updateEvent.counter))
            } else {
                // If the map in the stateFlow was replaced, emit it.
                this.emit(flowState.bundles)
            }
        }
    }.flattenConcat()

    suspend fun loadPatchClassesFiltered(packageName: String) =
        bundles.first()["official"]!!.loadPatchesFiltered(packageName)

    suspend fun getIntegrations() = bundles.first().values.mapNotNull { it.integrations }

}