package app.revanced.manager.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.data.room.apps.DownloadedApp
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.repository.DownloadedAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadsViewModel(
    private val downloadedAppRepository: DownloadedAppRepository,
    val prefs: PreferencesManager
) : ViewModel() {
    val downloadedApps = mutableStateListOf<DownloadedApp>()
    val selection = mutableStateListOf<DownloadedApp>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            downloadedAppRepository.getAll()
                .sortedWith(
                    compareBy<DownloadedApp> {
                        it.packageName
                    }.thenBy { it.version }
                )
                .also {
                    withContext(Dispatchers.Main) {
                        downloadedApps.addAll(it)
                    }
                }
        }
    }

    fun toggleItem(downloadedApp: DownloadedApp) {
        if (selection.contains(downloadedApp))
            selection.remove(downloadedApp)
        else
            selection.add(downloadedApp)
    }

    fun delete() {
        viewModelScope.launch(NonCancellable) {
            selection.toList().forEach {
                downloadedAppRepository.delete(it)
            }
            withContext(Dispatchers.Main) {
                downloadedApps.removeAll(selection)
                selection.clear()
            }
        }
    }

}