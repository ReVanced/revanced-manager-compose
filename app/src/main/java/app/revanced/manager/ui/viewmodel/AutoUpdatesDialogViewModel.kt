package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.bundles.RemotePatchBundle
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.repository.PatchBundleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AutoUpdatesDialogViewModel(
    prefs: PreferencesManager,
    private val bundleRepository: PatchBundleRepository
) : ViewModel() {
    val showDialog = prefs.showAutoUpdatesDialog
    private val managerAutoUpdates = prefs.managerAutoUpdates

    fun save(manager: Boolean, patches: Boolean) = viewModelScope.launch {
        showDialog.update(false)

        managerAutoUpdates.update(manager)
        if (patches) {
            with(bundleRepository) {
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