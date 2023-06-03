package app.revanced.manager.compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.compose.domain.repository.SourceRepository
import app.revanced.manager.compose.util.PM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    sourceRepository: SourceRepository,
    pm: PM
) : ViewModel() {
    init {
        viewModelScope.launch {
            sourceRepository.loadSources()
        }
        viewModelScope.launch {
            pm.getCompatibleApps()
        }
        viewModelScope.launch(Dispatchers.IO) {
            pm.getInstalledApps()
        }
    }
}