package app.revanced.manager.compose.patcher.worker

import androidx.work.Data
import androidx.work.workDataOf
import app.revanced.manager.compose.patcher.Session

// TODO: use kotlinx.serialization instead of this.
object ProgressUtil {
    fun toWorkData(progress: Session.Progress) = when (progress) {
        is Session.Progress.PatchSuccess -> workDataOf("patchName" to progress.patchName, "kind" to progress.kind.toString())
        else -> workDataOf("kind" to progress.kind.toString())
    }

    fun fromWorkData(workData: Data) = when (val kind = Session.ProgressKind.valueOf(workData.getString("kind")!!)) {
        Session.ProgressKind.SAVING -> Session.Progress.Saving
        Session.ProgressKind.MERGING -> Session.Progress.Merging
        Session.ProgressKind.UNPACKING -> Session.Progress.Unpacking
        Session.ProgressKind.PATCHING_START -> Session.Progress.PatchingStart
        Session.ProgressKind.PATCH_SUCCESS -> Session.Progress.PatchSuccess(workData.getString("patchName")!!)
    }
}