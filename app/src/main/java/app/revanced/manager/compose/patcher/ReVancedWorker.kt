package app.revanced.manager.compose.patcher

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.revanced.patcher.extensions.PatchExtensions.patchName
import org.koin.core.component.KoinComponent
import java.io.File

class ReVancedWorker(private val context: Context, parameters: WorkerParameters, private val patcherState: PatcherState): Worker(context, parameters), KoinComponent {
    override fun doWork(): Result {
        if (runAttemptCount > 0) {
            Log.d("revanced-worker", "Android requested retrying but retrying is disabled.")
            return Result.failure()
        }

        val input = inputData.getString("input")!!
        val output = inputData.getString("output")!!
        val selected = inputData.getStringArray("selectedPatches")!!.toSet()
        val pkgName = inputData.getString("packageName")!!
        val pkgVersion = inputData.getString("packageVersion")!!

        val patchList = patcherState.patchClassesFor(pkgName, pkgVersion).filter { selected.contains(it.patchName) }

        val session = Session(context.cacheDir.path, File(input))

        return try {
            session.run(File(output), patchList)
            Log.i("revanced-worker", "Patching succeeded")
            Result.success()
        } catch (e: Throwable) {
            Log.e("revanced-worker", "Got exception while patching", e)
            Result.failure()
        }
    }
}