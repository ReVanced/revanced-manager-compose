package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.data.room.apps.installed.InstalledApp
import app.revanced.manager.domain.repository.InstalledAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class InstalledAppsViewModel(
    private val installedAppsRepository: InstalledAppRepository
) : ViewModel() {
    val apps = installedAppsRepository.getAll().flowOn(Dispatchers.IO)

    fun delete(installedApp: InstalledApp) {
        viewModelScope.launch {
            installedAppsRepository.delete(installedApp)
        }
    }
}