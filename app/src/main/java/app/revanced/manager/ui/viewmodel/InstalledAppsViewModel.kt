package app.revanced.manager.ui.viewmodel

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.repository.InstalledAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class InstalledAppsViewModel(
    private val installedAppsRepository: InstalledAppRepository,
    private val app: Application
) : ViewModel() {
    val apps = installedAppsRepository.getAll().flowOn(Dispatchers.IO)

    val packageInfoMap = mutableStateMapOf<String, PackageInfo?>()

    init {
        viewModelScope.launch {
            apps.collect {
                it.forEach { installedApp ->
                    withContext(Dispatchers.Main) {
                        packageInfoMap[installedApp.currentPackageName] = withContext(Dispatchers.IO) {
                            try {
                                app.packageManager.getPackageInfo(installedApp.currentPackageName, 0)
                            } catch (_: NameNotFoundException) {
                                installedAppsRepository.delete(installedApp)
                                null
                            }
                        }
                    }
                }
            }
        }
    }
}