package app.revanced.manager.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.repository.DownloadedAppRepository
import app.revanced.manager.domain.repository.SourceRepository
import app.revanced.manager.network.downloader.APKMirror
import app.revanced.manager.network.downloader.AppDownloader
import app.revanced.manager.ui.model.SelectedApp
import app.revanced.manager.util.mutableStateSetOf
import app.revanced.manager.util.simpleMessage
import app.revanced.manager.util.tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class VersionSelectorViewModel(
    val packageName: String
) : ViewModel(), KoinComponent {
    private val downloadedAppRepository: DownloadedAppRepository = get()
    private val sourceRepository: SourceRepository = get()
    private val appDownloader: AppDownloader = APKMirror()

    var isDownloading: Boolean by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set

    val downloadableVersions = mutableStateSetOf<SelectedApp.Download>()

    val compatibleVersions = sourceRepository.bundles.map { bundles ->
        var patchesWithoutVersions = 0

        bundles.flatMap { (_, bundle) ->
            bundle.patches.flatMap { patch ->
                patch.compatiblePackages.orEmpty()
                    .filter { it.packageName == packageName }
                    .onEach { if (it.versions.isEmpty()) patchesWithoutVersions++ }
                    .flatMap { it.versions }
            }
        }.groupingBy { it }
            .eachCount()
            .toMutableMap()
            .apply {
                replaceAll { _, count ->
                    count + patchesWithoutVersions
                }
            }
    }

    val downloadedVersions = downloadedAppRepository.getAll().map { downloadedApps ->
        downloadedApps.filter { it.packageName == packageName }.map { SelectedApp.Local(it.packageName, it.version, it.file) }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val compatibleVersions = compatibleVersions.first()

                appDownloader.getAvailableVersions(
                    packageName,
                    compatibleVersions.keys
                ).collect {
                    if (it.version in compatibleVersions || compatibleVersions.isEmpty()) {
                        downloadableVersions.add(
                            SelectedApp.Download(
                                packageName,
                                it.version,
                                it
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(tag, "Failed to load apps", e)
                    errorMessage = e.simpleMessage()
                }
            }
        }
    }
}