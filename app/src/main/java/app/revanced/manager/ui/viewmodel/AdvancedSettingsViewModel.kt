package app.revanced.manager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.R
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.repository.PatchBundleRepository
import app.revanced.manager.domain.bundles.RemotePatchBundle
import app.revanced.manager.util.uiSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdvancedSettingsViewModel(
    val prefs: PreferencesManager,
    private val app: Application,
    private val patchBundleRepository: PatchBundleRepository
) : ViewModel() {
    val apiUrl = prefs.api

    fun setApiUrl(value: String) = viewModelScope.launch(Dispatchers.Default) {
        if (value == apiUrl.get()) return@launch

        apiUrl.update(value)
        patchBundleRepository.reloadApiBundles()
    }

    fun redownloadBundles() = viewModelScope.launch {
        uiSafe(app, R.string.source_download_fail, RemotePatchBundle.updateFailMsg) {
            patchBundleRepository.redownloadRemoteBundles()
        }
    }

    fun resetBundles() = viewModelScope.launch {
        patchBundleRepository.reset()
    }

    fun setInstaller(installer: PreferencesManager.InstallerManager) = viewModelScope.launch {
        prefs.defaultInstaller.update(installer)
    }
}