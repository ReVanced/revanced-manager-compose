package app.revanced.manager.compose.domain.repository

import androidx.lifecycle.LifecycleOwner
import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import app.revanced.manager.compose.util.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.*

class BundleRepository(sourcesProvider: SourcesProvider) {
    /**
     * A [Flow] that emits whenever the sources change.
     *
     * The outer flow emits whenever the sources configuration changes.
     * The inner flow emits whenever one of the bundles update.
     */
    private val sourceUpdates = sourcesProvider.sources.map { sources ->
        sources.map { (name, source) ->
            source.bundle.map { bundle ->
                name to bundle
            }
        }.merge().buffer()
    }

    private val _bundles = MutableStateFlow<Map<String, PatchBundle>>(emptyMap())
    val bundles = _bundles.asStateFlow()

    fun onAppStart(lifecycleOwner: LifecycleOwner) = lifecycleOwner.launchAndRepeatWithViewLifecycle {
        sourceUpdates.collect { events ->
            val map = HashMap<String, PatchBundle>()
            _bundles.emit(map)

            events.collect { (name, new) ->
                map[name] = new
                _bundles.emit(map)
            }
        }
    }
}