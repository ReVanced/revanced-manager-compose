package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.repository.PatchBundleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    patchBundleRepository: PatchBundleRepository
) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.Default) {
            patchBundleRepository.load()
            patchBundleRepository.updateCheck()
        }
    }
}