package app.revanced.manager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.R
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.repository.SourceRepository
import app.revanced.manager.domain.sources.RemoteSource
import app.revanced.manager.util.uiSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdvancedSettingsViewModel(
    prefs: PreferencesManager,
    private val app: Application,
    private val sourceRepository: SourceRepository
) : ViewModel() {
    val apiUrl = prefs.api

    fun setApiUrl(value: String) = viewModelScope.launch(Dispatchers.Default) {
        if (value == apiUrl.get()) return@launch

        apiUrl.update(value)
        sourceRepository.onApiUrlChange()
    }

    fun redownloadBundles() = viewModelScope.launch {
        uiSafe(app, R.string.source_download_fail, RemoteSource.updateFailMsg) {
            sourceRepository.redownloadRemoteSources()
        }
    }

    fun resetBundles() = viewModelScope.launch {
        sourceRepository.resetConfig()
    }
}