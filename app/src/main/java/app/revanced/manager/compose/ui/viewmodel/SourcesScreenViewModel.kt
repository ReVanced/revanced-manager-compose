package app.revanced.manager.compose.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.compose.domain.repository.SourcesProvider
import app.revanced.manager.compose.util.toast
import kotlinx.coroutines.launch

class SourcesScreenViewModel(private val app: Application, private val sourcesProvider: SourcesProvider) : ViewModel() {
    val sources = sourcesProvider.sources

    fun update() = viewModelScope.launch {
        try {
            sourcesProvider.reloadSources()
        } catch (err: Throwable) {
            app.toast("Failed to update patch bundles")
            Log.e("revanced-manager", "Failed to update patch bundles", err)
        }
    }
}