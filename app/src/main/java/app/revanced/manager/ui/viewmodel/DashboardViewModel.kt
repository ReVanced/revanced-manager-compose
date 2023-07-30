package app.revanced.manager.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.repository.SourceRepository
import io.ktor.http.Url
import kotlinx.coroutines.launch

class DashboardViewModel(
    app: Application,
    private val sourceRepository: SourceRepository
) : ViewModel() {
    val sources = sourceRepository.sources
    private val contentResolver: ContentResolver = app.contentResolver

    fun createLocalSource(name: String, patchBundle: Uri, integrations: Uri?) = viewModelScope.launch {
        contentResolver.openInputStream(patchBundle)!!.use { patchesStream ->
            val integrationsStream = integrations?.let { contentResolver.openInputStream(it) }
            try {
                sourceRepository.createLocalSource(name, patchesStream, integrationsStream)
            } finally {
                integrationsStream?.close()
            }
        }
    }

    fun createRemoteSource(name: String, apiUrl: Url, autoUpdate: Boolean) =
        viewModelScope.launch { sourceRepository.createRemoteSource(name, apiUrl, autoUpdate) }
}