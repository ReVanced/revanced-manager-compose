package app.revanced.manager.compose.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.*
import app.revanced.manager.compose.patcher.worker.PatcherWorker
import app.revanced.manager.compose.patcher.Session
import app.revanced.manager.compose.service.InstallService
import app.revanced.manager.compose.service.UninstallService
import app.revanced.manager.compose.util.PM
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files

class InstallerScreenViewModel(
    input: Uri,
    selectedPatches: List<String>,
    val app: Application
) : ViewModel() {

    sealed class Status(val header: String) {
        override fun toString() = header

        object Idle : Status("Idle")
        object Starting : Status("Starting")
        object Success : Status("Success")
        object Failure : Status("Failed")
        data class Patching(val progress: Session.Progress) : Status(progress.toString())
    }

    val workManager = WorkManager.getInstance(app)
    var installStatus by mutableStateOf<Boolean?>(null)
    var pmStatus by mutableStateOf(-999)
    var extra by mutableStateOf("")

    val outputFile = File(app.cacheDir, "output.apk")
    val inputFile = app.contentResolver.openInputStream(input)!!.use { stream ->
        File(app.cacheDir, "input.apk").also {
            if (it.exists()) it.delete()
            Files.copy(stream, it.toPath())
        }
    }

    private val patcherWorker =
        OneTimeWorkRequest.Builder(PatcherWorker::class.java) // create Worker
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).setInputData(
                Data.Builder().putString(
                    "args",
                    Json.Default.encodeToString(
                        PatcherWorker.Args(
                            inputFile.path,
                            outputFile.path,
                            selectedPatches,
                            "amog.us",
                            "0.69.420"
                        )
                    )
                ).build()
            ).build()

    private val liveData = workManager.getWorkInfoByIdLiveData(patcherWorker.id) // get LiveData

    private val observer = Observer { workInfo: WorkInfo -> // observer for observing patch status
        status = when (workInfo.state) {
            WorkInfo.State.RUNNING -> workInfo.progress.getString(PatcherWorker.Progress)
                ?.let { Status.Patching(Session.Progress.valueOf(it)) } ?: Status.Starting

            WorkInfo.State.SUCCEEDED -> Status.Success
            WorkInfo.State.FAILED -> Status.Failure
            else -> Status.Idle
        }
    }
    var status by mutableStateOf<Status>(Status.Idle)

    private val installBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                InstallService.APP_INSTALL_ACTION -> {
                    pmStatus = intent.getIntExtra(InstallService.EXTRA_INSTALL_STATUS, -999)
                    extra = intent.getStringExtra(InstallService.EXTRA_INSTALL_STATUS_MESSAGE)!!
                    postInstallStatus()
                }

                UninstallService.APP_UNINSTALL_ACTION -> {
                }
            }
        }
    }

    init {
        workManager.enqueueUniqueWork("patching", ExistingWorkPolicy.KEEP, patcherWorker)
        liveData.observeForever(observer)
        app.registerReceiver(installBroadcastReceiver, IntentFilter().apply {
            addAction(InstallService.APP_INSTALL_ACTION)
            addAction(UninstallService.APP_UNINSTALL_ACTION)
        })
    }

    fun installApk(apk: File) {
        PM.installApp(apk, app)
    }

    fun postInstallStatus() {
        installStatus = pmStatus == PackageInstaller.STATUS_SUCCESS
    }

    override fun onCleared() {
        super.onCleared()
        liveData.removeObserver(observer)
        app.unregisterReceiver(installBroadcastReceiver)
        workManager.cancelWorkById(patcherWorker.id)
        // logs.clear()
    }
}