package app.revanced.manager.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import app.revanced.manager.R
import app.revanced.manager.domain.manager.KeystoreManager
import app.revanced.manager.domain.worker.WorkerRepository
import app.revanced.manager.patcher.worker.PatcherProgressManager
import app.revanced.manager.patcher.worker.PatcherWorker
import app.revanced.manager.patcher.worker.Step
import app.revanced.manager.service.InstallService
import app.revanced.manager.service.UninstallService
import app.revanced.manager.ui.destination.Destination
import app.revanced.manager.util.PM
import app.revanced.manager.util.tag
import app.revanced.manager.util.toast
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Files
import java.util.UUID

@Stable
class InstallerViewModel(input: Destination.Installer) : ViewModel(), KoinComponent {
    private val keystoreManager: KeystoreManager by inject()
    private val app: Application by inject()
    private val pm: PM by inject()
    private val workerRepository: WorkerRepository by inject()

    val packageName: String = input.app.packageName
    private val outputFile = File(app.cacheDir, "output.apk")
    private val signedFile = File(app.cacheDir, "signed.apk").also { if (it.exists()) it.delete() }
    private var hasSigned = false

    var isInstalling by mutableStateOf(false)
        private set
    var installedPackageName by mutableStateOf<String?>(null)
        private set
    val appButtonText by derivedStateOf { if (installedPackageName == null) R.string.install_app else R.string.open_app }

    private val workManager = WorkManager.getInstance(app)

    private val _progress: MutableStateFlow<ImmutableList<Step>>
    private val patcherWorkerId: UUID

    init {
        val (appInfo, patches, options) = input

        _progress = MutableStateFlow(PatcherProgressManager.generateSteps(
            app,
            patches.flatMap { (_, selected) -> selected }
        ).toImmutableList())
        patcherWorkerId =
            workerRepository.launchExpedited<PatcherWorker, PatcherWorker.Args>(
                "patching", PatcherWorker.Args(
                    appInfo.path!!.absolutePath,
                    outputFile.path,
                    patches,
                    options,
                    packageName,
                    appInfo.packageInfo!!.versionName,
                    _progress
                )
            )
    }

    val progress = _progress.asStateFlow()

    val patcherState =
        workManager.getWorkInfoByIdLiveData(patcherWorkerId).map { workInfo: WorkInfo ->
            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> true
                WorkInfo.State.FAILED -> false
                else -> null
            }
        }

    private val installBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                InstallService.APP_INSTALL_ACTION -> {
                    val pmStatus = intent.getIntExtra(InstallService.EXTRA_INSTALL_STATUS, -999)
                    val extra = intent.getStringExtra(InstallService.EXTRA_INSTALL_STATUS_MESSAGE)!!

                    if (pmStatus == PackageInstaller.STATUS_SUCCESS) {
                        app.toast(app.getString(R.string.install_app_success))
                        installedPackageName =
                            intent.getStringExtra(InstallService.EXTRA_PACKAGE_NAME)
                    } else {
                        app.toast(app.getString(R.string.install_app_fail, extra))
                    }
                }

                UninstallService.APP_UNINSTALL_ACTION -> {
                }
            }
        }
    }

    init {
        app.registerReceiver(installBroadcastReceiver, IntentFilter().apply {
            addAction(InstallService.APP_INSTALL_ACTION)
            addAction(UninstallService.APP_UNINSTALL_ACTION)
        })
    }

    override fun onCleared() {
        super.onCleared()
        app.unregisterReceiver(installBroadcastReceiver)
        workManager.cancelWorkById(patcherWorkerId)

        outputFile.delete()
        signedFile.delete()
    }

    private suspend fun signApk(): Boolean {
        if (!hasSigned) {
            try {
                keystoreManager.sign(outputFile, signedFile)
            } catch (e: Exception) {
                Log.e(tag, "Got exception while signing", e)
                app.toast(app.getString(R.string.sign_fail, e::class.simpleName))
                return false
            }
        }

        return true
    }

    fun export(uri: Uri?) = uri?.let {
        viewModelScope.launch {
            if (signApk()) {
                withContext(Dispatchers.IO) {
                    Files.copy(signedFile.toPath(), app.contentResolver.openOutputStream(it))
                }
                app.toast(app.getString(R.string.export_app_success))
            }
        }
    }

    fun installOrOpen() = viewModelScope.launch {
        installedPackageName?.let {
            pm.launch(it)
            return@launch
        }

        isInstalling = true
        try {
            if (!signApk()) return@launch
            pm.installApp(listOf(signedFile))
        } finally {
            isInstalling = false
        }
    }
}