package app.revanced.manager.service

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import app.revanced.manager.util.PM
import rikka.shizuku.Shizuku
import java.io.File

class ShizukuService(
    private val app: Application,
    private val pm: PM
) {
    var isPermissionGranted by mutableStateOf(false)

    init {
        Shizuku.addBinderReceivedListenerSticky {
            isPermissionGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
        Shizuku.addBinderDeadListener {
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

    fun isShizukuInstalled() = pm.getPackageInfo(shizukuPackageName) != null

    companion object {
        const val shizukuPackageName = "moe.shizuku.privileged.api"
    }
}