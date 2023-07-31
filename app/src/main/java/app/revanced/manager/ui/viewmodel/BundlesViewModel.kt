package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.bundles.BundleSource
import app.revanced.manager.domain.repository.PatchBundleRepository
import kotlinx.coroutines.launch

class BundlesViewModel(
    private val patchBundleRepository: PatchBundleRepository
) : ViewModel() {
    val sources = patchBundleRepository.sources

    fun delete(bundle: BundleSource) = viewModelScope.launch { patchBundleRepository.remove(bundle) }
}