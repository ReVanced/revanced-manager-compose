package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.sources.Source
import app.revanced.manager.domain.repository.SourceRepository
import kotlinx.coroutines.launch

class BundlesViewModel(
    private val sourceRepository: SourceRepository
) : ViewModel() {
    val sources = sourceRepository.sources

    fun delete(source: Source) = viewModelScope.launch { sourceRepository.remove(source) }
}