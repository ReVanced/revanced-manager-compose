package app.revanced.manager.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.repository.SourceRepository
import app.revanced.manager.network.downloader.AppDownloader
import app.revanced.manager.network.downloader.APKMirror
import app.revanced.manager.util.AppInfo
import app.revanced.manager.util.PM
import app.revanced.manager.util.tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AppDownloaderViewModel(
    private val selectedApp: AppInfo
) : ViewModel(), KoinComponent {
    private val sourceRepository: SourceRepository = get()
    private val pm: PM = get()
    val appDownloader: AppDownloader = get<APKMirror>()

    var isDownloading: String? by mutableStateOf(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set

    val compatibleVersions = HashMap<String, Int>()

    private val job = viewModelScope.launch(Dispatchers.IO) {
        try {
            compatibleVersions.putAll(getCompatibleVersions())

            appDownloader.getAvailableVersionList(
                selectedApp.packageName,
                compatibleVersions.keys
            )

            withContext(Dispatchers.Main) {
                isLoading = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage = e.message ?: e.cause?.message ?: e::class.simpleName
                Log.e(tag, "Failed to load app info", e)
            }
        }
    }

    private suspend fun getCompatibleVersions(): HashMap<String, Int> {
        val map = HashMap<String, Int>()

        sourceRepository.bundles.first().flatMap { it.value.patches }.forEach { patch ->
            patch.compatiblePackages?.find { it.name == selectedApp.packageName }
                ?.let { compatiblePackage ->
                    compatiblePackage.versions.forEach { compatibleVersion ->
                        map[compatibleVersion] = map.getOrDefault(compatibleVersion, 0) + 1
                    }
                }
        }

        return map
    }

    fun downloadApp(
        link: String,
        onComplete: (AppInfo) -> Unit
    ) {
        isDownloading = link

        job.cancel()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadedFile = appDownloader.downloadApp(link)

                val apkInfo = pm.getApkInfo(downloadedFile) ?: throw Exception("Failed to load apk info")

                withContext(Dispatchers.Main) {
                    onComplete(apkInfo)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message ?: e.cause?.message ?: e::class.simpleName
                    Log.e(tag, "Failed to download apk", e)
                }
            }
        }
    }
}