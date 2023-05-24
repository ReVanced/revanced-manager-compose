package app.revanced.manager.compose.ui.viewmodel

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.compose.network.api.ManagerAPI
import app.revanced.manager.compose.network.dto.Changelog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.revanced.manager.compose.util.PM
import java.io.File

class UpdateSettingsViewModel(
    private val managerAPI: ManagerAPI,
    private val app: Application,
) : ViewModel() {
    val downloadProgress get() = (managerAPI.downloadProgress?.times(100)) ?: 0f
    val downloadedSize get() = managerAPI.downloadedSize ?: 0L
    val totalSize get() = managerAPI.totalSize ?: 0L
    var changelog by mutableStateOf(Changelog("0","Loading changelog",0))
        private set
    fun downloadLatestManager() {
        viewModelScope.launch(Dispatchers.IO) {
            managerAPI.downloadManager()
        }
    }

     fun getChangelog() {
        viewModelScope.launch(Dispatchers.IO) {
            changelog = managerAPI.fetchChangelog("revanced-manager")
        }
    }
    fun installUpdate() {
        PM.installApp(
            apks = listOf(
                File(
                    (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/revanced-manager.apk")
                        .toString())
                ),
            ),
            context = app,
        )
    }
}