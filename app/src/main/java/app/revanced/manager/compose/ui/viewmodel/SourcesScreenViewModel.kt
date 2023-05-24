package app.revanced.manager.compose.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.compose.R
import app.revanced.manager.compose.domain.repository.SourcesProvider
import app.revanced.manager.compose.util.toast
import io.ktor.http.*
import kotlinx.coroutines.launch

class SourcesScreenViewModel(private val app: Application, private val sourcesProvider: SourcesProvider) : ViewModel() {
    val sources = sourcesProvider.sources
    var showNewSourceDialog by mutableStateOf(false)
    private val contentResolver: ContentResolver = app.contentResolver

    fun doUpdate(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (err: Throwable) {
            app.toast(app.getString(R.string.source_download_fail, err.message))
            Log.e("revanced-manager", "Failed to update patch bundles", err)
        }
    }

    fun redownloadAllSources() = doUpdate { sourcesProvider.redownloadRemoteSources() }

    suspend fun addLocal(name: String, patchBundle: Uri, integrations: Uri?) {
        contentResolver.openInputStream(patchBundle)!!.use { patchesStream ->
            val integrationsStream = integrations?.let { contentResolver.openInputStream(it) }
            try {
                sourcesProvider.createLocalSource(name, patchesStream, integrationsStream)
            } finally {
                integrationsStream?.close()
            }
        }
    }

    suspend fun addRemote(name: String, apiUrl: Url) = sourcesProvider.createRemoteSource(name, apiUrl)

    fun deleteAllSources() = viewModelScope.launch {
        sourcesProvider.resetConfig()
    }
}