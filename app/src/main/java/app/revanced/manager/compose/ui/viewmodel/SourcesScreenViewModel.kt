package app.revanced.manager.compose.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.compose.R
import app.revanced.manager.compose.domain.repository.SourcesProvider
import app.revanced.manager.compose.util.toast
import kotlinx.coroutines.launch

class SourcesScreenViewModel(private val app: Application, private val sourcesProvider: SourcesProvider) : ViewModel() {
    val sources = sourcesProvider.sources

    fun doUpdate(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (err: Throwable) {
            app.toast(app.getString(R.string.source_download_fail, err.message))
            Log.e("revanced-manager", "Failed to update patch bundles", err)
        }
    }

    fun update() = doUpdate { sourcesProvider.reloadSources() }
}