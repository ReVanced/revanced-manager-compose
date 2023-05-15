package app.revanced.manager.compose.patcher

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.revanced.patcher.extensions.PatchExtensions.patchName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import java.io.File

class ReVancedWorker(private val context: Context, parameters: WorkerParameters, private val patcherState: PatcherState): Worker(context, parameters), KoinComponent {
    @Serializable
    data class Args(val input: String, val output: String, val selectedPatches: List<String>, val packageName: String, val packageVersion: String)
    override fun doWork(): Result {
        if (runAttemptCount > 0) {
            Log.d("revanced-worker", "Android requested retrying but retrying is disabled.")
            return Result.failure()
        }

        val args = Json.decodeFromString<Args>(inputData.getString("args")!!)
        val selected = args.selectedPatches.toSet()

        val patchList = patcherState.patchClassesFor(args.packageName, args.packageVersion).filter { selected.contains(it.patchName) }

        val session = Session(context.cacheDir.path, File(args.input))

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