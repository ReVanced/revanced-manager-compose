package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.repository.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdvancedSettingsViewModel(
    prefs: PreferencesManager,
    private val sourceRepository: SourceRepository
) : ViewModel() {
    val apiUrl = prefs.api

    fun setApiUrl(value: String) = viewModelScope.launch(Dispatchers.Default) {
        if (value == apiUrl.get()) return@launch

        apiUrl.update(value)
        sourceRepository.onApiUrlChange()
    }
}