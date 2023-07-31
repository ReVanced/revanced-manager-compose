package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.bundles.RemotePatchBundle
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.repository.PatchBundleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val patchBundleRepository: PatchBundleRepository,
    val prefs: PreferencesManager
) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.Default) {
            with(patchBundleRepository) {
                load()
                updateCheck()
            }
        }
    }

    fun applyAutoUpdatePrefs(manager: Boolean, patches: Boolean) = viewModelScope.launch {
        prefs.showAutoUpdatesDialog.update(false)

        prefs.managerAutoUpdates.update(manager)
        if (patches) {
            with(patchBundleRepository) {
                sources
                    .first()
                    .find { it.uid == 0 }
                    ?.let { it as? RemotePatchBundle }
                    ?.setAutoUpdate(true)

                updateCheck()
            }
        }
    }
}