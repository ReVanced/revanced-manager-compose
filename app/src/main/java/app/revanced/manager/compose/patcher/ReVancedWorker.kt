package app.revanced.manager.compose.patcher

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import java.io.File

class ReVancedWorker(context: Context, parameters: WorkerParameters, private val output: File, private val session: Session, private val selectedPatches: PatchList): Worker(context, parameters), KoinComponent {
    override fun doWork(): Result {
        if (runAttemptCount > 0) {
            Log.d("revanced-worker", "Android requested retrying but retrying is disabled.")
            return Result.failure()
        }

        return try {
            session.run(output, selectedPatches)
            Log.i("revanced-worker", "Patching succeeded")
            Result.success()
        } catch (e: Throwable) {
            Log.e("revanced-worker", "Got exception while patching", e)
            Result.failure()
        }
    }
}