package app.revanced.manager.util

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.R)
object RequestManageStorageContract : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String) = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

    override fun getSynchronousResult(context: Context, input: String): SynchronousResult<Boolean>? = if (Environment.isExternalStorageManager()) SynchronousResult(true) else null

    override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK && Environment.isExternalStorageManager()
}