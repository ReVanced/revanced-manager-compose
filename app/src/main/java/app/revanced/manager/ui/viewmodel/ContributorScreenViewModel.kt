package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import app.revanced.manager.domain.repository.ReVancedRepository
import app.revanced.manager.network.utils.getOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ContributorScreenViewModel(private val repository: ReVancedRepository): ViewModel() {
    fun getRepositories(): List<app.revanced.manager.network.dto.ReVancedRepository> {
        var repositories: List<app.revanced.manager.network.dto.ReVancedRepository> = listOf()

        runBlocking {
            launch {
                repositories = repository.getContributors().getOrNull()?.repositories ?: listOf()
            }
        }

        return repositories
    }
}