package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.bundles.PatchBundleSource
import app.revanced.manager.domain.repository.PatchBundleRepository
import kotlinx.coroutines.launch

class BundlesViewModel(
    private val patchBundleRepository: PatchBundleRepository
) : ViewModel() {
    val sources = patchBundleRepository.sources

    fun delete(bundle: PatchBundleSource) = viewModelScope.launch { patchBundleRepository.remove(bundle) }
}