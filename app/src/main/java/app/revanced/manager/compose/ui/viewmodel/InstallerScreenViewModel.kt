package app.revanced.manager.compose.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.*
import app.revanced.manager.compose.patcher.worker.PatcherWorker
import app.revanced.manager.compose.patcher.Session
import app.revanced.manager.compose.patcher.worker.ProgressUtil
import app.revanced.manager.compose.service.InstallService
import app.revanced.manager.compose.service.UninstallService
import app.revanced.manager.compose.util.PM
import app.revanced.manager.compose.util.PackageInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class InstallerScreenViewModel(
    input: PackageInfo,
    selectedPatches: List<String>,
    val app: Application
) : ViewModel() {
    enum class StepStatus {
        WAITING,
        COMPLETED,
        FAILURE,
    }

    class Step(val name: String, val status: StepStatus = StepStatus.WAITING)

    class StepGroup(val name: String, val steps: List<Step>, val status: StepStatus = StepStatus.WAITING)

    // TODO: expose this as immutable or something.
    val stepGroups = mutableStateListOf(
        StepGroup("Preparation", listOf(Step("Unpack apk"), Step("Merge integrations"))),
        StepGroup(
            "Patching",
            selectedPatches.map { Step(it) }
        ),
        StepGroup("Saving", listOf(Step("Write patched apk")))
    )

    private companion object {
        const val PATCHING_GROUP_INDEX = 1
    }

    /**
     * A map of [Session.ProgressKind] to the corresponding position in [stepGroups]
     */
    private val stepKeyMap = mapOf(
        Session.ProgressKind.UNPACKING to StepKey(0, 0),
        Session.ProgressKind.MERGING to StepKey(0, 1),
        Session.ProgressKind.PATCHING_START to StepKey(PATCHING_GROUP_INDEX, 0),
        Session.ProgressKind.SAVING to StepKey(2, 0),
    )

    private var currentStep: StepKey? = null

    private fun <T> MutableList<T>.mutateIndex(index: Int, callback: (T) -> T) = apply {
        this[index] = callback(this[index])
    }

    private fun updateStepStatus(key: StepKey, newStatus: StepStatus) {
        var isLastStepOfGroup = false
        stepGroups.mutateIndex(key.groupIndex) { group ->
            isLastStepOfGroup = key.stepIndex == group.steps.size - 1
            val newGroupStatus = when {
                // This group failed if a step in it failed.
                newStatus == StepStatus.FAILURE -> StepStatus.FAILURE
                // All steps in the group succeeded.
                newStatus == StepStatus.COMPLETED && isLastStepOfGroup -> StepStatus.COMPLETED
                // Keep the old status.
                else -> group.status
            }

            StepGroup(group.name, group.steps.toMutableList().mutateIndex(key.stepIndex) { step ->
                Step(step.name, newStatus)
            }, newGroupStatus)
        }

        val isFinalStep = isLastStepOfGroup && key.groupIndex == stepGroups.size - 1

        if (newStatus == StepStatus.COMPLETED) {
            // Move the cursor to the next step.
            currentStep = when {
                isFinalStep -> null // Final step has been completed.
                isLastStepOfGroup -> StepKey(key.groupIndex + 1, 0) // Move to the next group.
                else -> StepKey(key.groupIndex, key.stepIndex + 1) // Move to the next step of this group.
            }
        }
    }

    private data class StepKey(val groupIndex: Int, val stepIndex: Int)

    private val workManager = WorkManager.getInstance(app)

    // TODO: handle app installation as a step.
    var installStatus by mutableStateOf<Boolean?>(null)
    var pmStatus by mutableStateOf(-999)
    var extra by mutableStateOf("")

    private val outputFile = File(app.cacheDir, "output.apk")

    private val patcherWorker =
        OneTimeWorkRequest.Builder(PatcherWorker::class.java) // create Worker
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).setInputData(
                Data.Builder().putString(
                    "args",
                    Json.Default.encodeToString(
                        PatcherWorker.Args(
                            input.apk.path,
                            outputFile.path,
                            selectedPatches,
                            input.packageName,
                            input.packageName,
                        )
                    )
                ).build()
            ).build()

    private val liveData = workManager.getWorkInfoByIdLiveData(patcherWorker.id) // get LiveData

    private val observer = Observer { workInfo: WorkInfo -> // observer for observing patch status
        when (workInfo.state) {
            WorkInfo.State.RUNNING -> workInfo.progress.getString("kind")?.let {
                val progress = ProgressUtil.fromWorkData(workInfo.progress)

                if (progress is Session.Progress.PatchSuccess) {
                    val patchStepKey = StepKey(
                        PATCHING_GROUP_INDEX,
                        stepGroups[PATCHING_GROUP_INDEX].steps.indexOfFirst { it.name == progress.patchName })

                    updateStepStatus(patchStepKey, StepStatus.COMPLETED)
                } else {
                    currentStep?.let { updateStepStatus(it, StepStatus.COMPLETED) }

                    currentStep = stepKeyMap[progress.kind]!!
                }
            }

            WorkInfo.State.FAILED -> {
                // TODO: retrieve the error and "associate it" with the step that just failed.
                currentStep?.let { updateStepStatus(it, StepStatus.FAILURE) }
            }

            WorkInfo.State.SUCCEEDED -> {
                currentStep?.let { updateStepStatus(it, StepStatus.COMPLETED) }
            }

            else -> {}
        }
    }

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

    fun installApk(apk: List<File>) {
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