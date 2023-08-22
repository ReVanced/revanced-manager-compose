package app.revanced.manager.service

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import rikka.shizuku.Shizuku
import java.io.File

class ShizukuApi(
    private val app: Application
) {

    var isBinderAvailable = false
    var isPermissionGranted by mutableStateOf(false)

    init {
        Shizuku.addBinderReceivedListenerSticky {
            isBinderAvailable = true
            isPermissionGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
        Shizuku.addBinderDeadListener {
            isBinderAvailable = false
            isPermissionGranted = false
        }
    }

    fun installPackage(file: File) {
        val intent = Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(app, "app.revanced.manager.provider", file)
        ).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        app.startActivity(intent)
    }

    fun isShizukuPermissionGranted() = isBinderAvailable && isPermissionGranted

    fun isShizukuInstalled() = Shizuku.pingBinder()
}