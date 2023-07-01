package app.revanced.manager.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.repository.DownloadedAppRepository
import app.revanced.manager.domain.repository.SourceRepository
import app.revanced.manager.network.downloader.APKMirror
import app.revanced.manager.network.downloader.AppDownloader
import app.revanced.manager.util.AppInfo
import app.revanced.manager.util.PM
import app.revanced.manager.util.simpleMessage
import app.revanced.manager.util.tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AppDownloaderViewModel(
    private val selectedApp: AppInfo
) : ViewModel(), KoinComponent {
    private val app: Application = get()
    private val downloadedAppRepository: DownloadedAppRepository = get()
    private val sourceRepository: SourceRepository = get()
    private val pm: PM = get()
    private val prefs: PreferencesManager = get()
    val appDownloader: AppDownloader = get<APKMirror>()

    var isDownloading: Boolean by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set

    val compatibleVersions = sourceRepository.bundles.map { bundles ->
        var patchesWithoutVersions = 0

        bundles.flatMap { bundle ->
            bundle.value.patches.flatMap { patch ->
                patch.compatiblePackages
                    .orEmpty()
                    .filter { it.name == selectedApp.packageName }
                    .onEach {
                        if (it.versions.isEmpty()) patchesWithoutVersions += 1
                    }
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
        downloadedApps.mapNotNull {
            if (it.packageName == selectedApp.packageName)
                it.version
            else
                null
        }
    }

    private val job = viewModelScope.launch(Dispatchers.IO) {
        try {
            appDownloader.getAvailableVersionList(
                selectedApp.packageName,
                compatibleVersions.first().keys
            )

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

    lateinit var onComplete: (AppInfo) -> Unit

    fun downloadApp(
        version: String,
        link: String
    ) {
        isDownloading = true

        job.cancel()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savePath = app.filesDir.resolve("downloaded-apps").resolve(selectedApp.packageName).also { it.mkdirs() }

                val downloadedFile =
                    downloadedAppRepository.get(selectedApp.packageName, version)?.file
                        ?: appDownloader.downloadApp(
                            link,
                            version,
                            savePath,
                            preferSplit = prefs.preferSplits,
                            preferUniversal = prefs.preferUniversal
                        ).also {
                            downloadedAppRepository.add(
                                selectedApp.packageName,
                                version,
                                it
                            )
                        }

                val apkInfo = pm.getApkInfo(downloadedFile)
                    ?: throw Exception("Failed to load apk info")

                withContext(Dispatchers.Main) {
                    onComplete(apkInfo)
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    Log.e(tag, "Failed to download apk", e)
                    errorMessage = e.simpleMessage()
                }
            }
        }
    }
}