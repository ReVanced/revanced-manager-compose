package app.revanced.manager.compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.compose.network.api.ManagerAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateSettingsViewModel(
    private val managerAPI: ManagerAPI
) : ViewModel() {
    val downloadProgress get() = (managerAPI.downloadProgress?.times(100)) ?: 0f
    fun downloadLatestManager() {
        viewModelScope.launch(Dispatchers.IO) {
            managerAPI.downloadManager()
        }
    }
}