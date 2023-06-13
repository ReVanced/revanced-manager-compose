package app.revanced.manager.patcher.worker

import android.content.Context
import androidx.annotation.StringRes
import androidx.work.Data
import androidx.work.workDataOf
import app.revanced.manager.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator

sealed class Progress {
    object Unpacking : Progress()
    object Merging : Progress()
    object PatchingStart : Progress()

    data class PatchSuccess(val patchName: String) : Progress()

    object Saving : Progress()
}

@Serializable
data class Cause(val className: String, val stacktrace: String)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("t")
sealed class StepStatus {
    @Serializable
    @SerialName("w")
    object Waiting : StepStatus()

    @Serializable
    @SerialName("c")
    object Completed : StepStatus()

    @Serializable
    @SerialName("f")
    data class Failure(val cause: Cause? = null) : StepStatus()
}

@Serializable
class Step(val name: String, val status: StepStatus = StepStatus.Waiting)

@Serializable
class StepGroup(
    @StringRes val name: Int,
    val steps: List<Step>,
    val status: StepStatus = StepStatus.Waiting
)

class PatcherProgressManager(context: Context, selectedPatches: List<String>) {
    val stepGroups = generateGroupsList(context, selectedPatches)

    companion object {
        private const val WORK_DATA_KEY = "progress"

        /**
         * A map of [Progress] to the corresponding position in [stepGroups]
         */
        private val stepKeyMap = mapOf(
            Progress.Unpacking to StepKey(0, 1),
            Progress.Merging to StepKey(0, 2),
            Progress.PatchingStart to StepKey(1, 0),
            Progress.Saving to StepKey(2, 0),
        )

        private fun generatePatchesGroup(selectedPatches: List<String>) = StepGroup(
            R.string.patcher_step_group_patching,
            selectedPatches.map { Step(it) }
        )

        fun generateGroupsList(context: Context, selectedPatches: List<String>) = mutableListOf(
            StepGroup(
                R.string.patcher_step_group_prepare,
                persistentListOf(
                    Step(context.getString(R.string.patcher_step_load_patches)),
                    Step(context.getString(R.string.patcher_step_unpack)),
                    Step(context.getString(R.string.patcher_step_integrations))
                )
            ),
            generatePatchesGroup(selectedPatches),
            StepGroup(
                R.string.patcher_step_group_saving,
                persistentListOf(Step(context.getString(R.string.patcher_step_write_patched)))
            )
        )

        fun groupsFromWorkData(workData: Data) = workData.getString(WORK_DATA_KEY)
            ?.let { Json.decodeFromString<List<StepGroup>>(it) }
    }

    fun groupsToWorkData() = workDataOf(WORK_DATA_KEY to Json.Default.encodeToString(stepGroups))

    private var currentStep: StepKey? = null

    private fun <T> MutableList<T>.mutateIndex(index: Int, callback: (T) -> T) = apply {
        this[index] = callback(this[index])
    }

    private fun updateStepStatus(key: StepKey, newStatus: StepStatus) {
        var isLastStepOfGroup = false
        stepGroups.mutateIndex(key.groupIndex) { group ->
            isLastStepOfGroup = key.stepIndex == group.steps.lastIndex

            val newGroupStatus = when {
                // This group failed if a step in it failed.
                newStatus is StepStatus.Failure -> StepStatus.Failure()
                // All steps in the group succeeded.
                newStatus is StepStatus.Completed && isLastStepOfGroup -> StepStatus.Completed
                // Keep the old status.
                else -> group.status
            }

            StepGroup(group.name, group.steps.toMutableList().mutateIndex(key.stepIndex) { step ->
                Step(step.name, newStatus)
            }, newGroupStatus)
        }

        val isFinalStep = isLastStepOfGroup && key.groupIndex == stepGroups.lastIndex

        if (newStatus is StepStatus.Completed) {
            // Move the cursor to the next step.
            currentStep = when {
                isFinalStep -> null // Final step has been completed.
                isLastStepOfGroup -> StepKey(key.groupIndex + 1, 0) // Move to the next group.
                else -> StepKey(
                    key.groupIndex,
                    key.stepIndex + 1
                ) // Move to the next step of this group.
            }
        }
    }

    fun replacePatchesList(newList: List<String>) {
        stepGroups[stepKeyMap[Progress.PatchingStart]!!.groupIndex] = generatePatchesGroup(newList)
    }

    private fun setCurrentStepStatus(newStatus: StepStatus) =
        currentStep?.let { updateStepStatus(it, newStatus) }

    private data class StepKey(val groupIndex: Int, val stepIndex: Int)

    fun handle(progress: Progress) = success().also {
        stepKeyMap[progress]?.let { currentStep = it }
    }

    fun failure() {
        setCurrentStepStatus(StepStatus.Failure())
    }
    fun failure(error: Throwable) {
        setCurrentStepStatus(StepStatus.Failure(Cause(error.javaClass.canonicalName ?: "unknown", error.stackTraceToString())))
    }

    fun success() {
        setCurrentStepStatus(StepStatus.Completed)
    }
}