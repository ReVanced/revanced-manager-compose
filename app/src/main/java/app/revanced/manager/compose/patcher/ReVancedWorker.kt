package app.revanced.manager.compose.patcher

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.revanced.manager.compose.patcher.aapt.Aapt
import app.revanced.patcher.extensions.PatchExtensions.patchName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileNotFoundException

class ReVancedWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters), KoinComponent {
    private val patcherState: PatcherState by inject()

    companion object {
        const val Progress = "progress"
    }

    @Serializable
    data class Args(
        val input: String,
        val output: String,
        val selectedPatches: List<String>,
        val packageName: String,
        val packageVersion: String
    )

    override suspend fun doWork(): Result {
        if (runAttemptCount > 0) {
            Log.d("revanced-worker", "Android requested retrying but retrying is disabled.")
            return Result.failure()
        }
        val aaptPath =
            Aapt.binary(applicationContext)?.absolutePath ?: throw FileNotFoundException("Could not resolve aapt.")

        val frameworkPath =
            applicationContext.cacheDir.resolve("framework").also { it.mkdirs() }.absolutePath

        val args = Json.decodeFromString<Args>(inputData.getString("args")!!)
        val selected = args.selectedPatches.toSet()

        val patchList = patcherState.patchClassesFor(args.packageName, args.packageVersion)
            .filter { selected.contains(it.patchName) }

        val session = Session(applicationContext.cacheDir.path, frameworkPath, aaptPath, File(args.input)) {
            setProgress(workDataOf(Progress to it.toString()))
        }

        return try {
            session.run(File(args.output), patchList)
            Log.i("revanced-worker", "Patching succeeded")
            Result.success()
        } catch (e: Throwable) {
            Log.e("revanced-worker", "Got exception while patching", e)
            Result.failure()
        }
    }
}