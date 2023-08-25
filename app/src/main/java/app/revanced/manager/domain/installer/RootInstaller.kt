package app.revanced.manager.domain.installer

import android.app.Application
import app.revanced.manager.service.RootConnection
import app.revanced.manager.util.PM
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class RootInstaller(
    private val app: Application,
    private val rootConnection: RootConnection,
    private val pm: PM
) {
    fun hasRootAccess() = Shell.isAppGrantedRoot() ?: false

    fun isAppInstalled(packageName: String) =
        rootConnection.remoteFS?.getFile("$modulesPath/$packageName-revanced/$packageName.apk")?.exists()

    fun isAppMounted(packageName: String): Boolean {
        return pm.getPackageInfo(packageName)?.applicationInfo?.sourceDir?.let {
            Shell.cmd("mount | grep \"$it\"").exec().isSuccess
        } ?: false
    }

    fun mount(packageName: String): Boolean {
        if (isAppMounted(packageName)) return true

        val stockAPK = pm.getPackageInfo(packageName)?.applicationInfo?.sourceDir
        val patchedAPK = "$modulesPath/$packageName-revanced/$packageName.apk"

        return stockAPK?.let {
            Shell.cmd("mount -o bind \"$patchedAPK\" \"$it\"").exec().isSuccess
        } ?: false
    }

    fun unmount(packageName: String): Boolean {
        val stockAPK = pm.getPackageInfo(packageName)

        return (stockAPK?.let { stockPath ->
            Shell.cmd("umount -l \"$stockPath\"").exec().isSuccess
        } ?: false)
    }

    suspend fun install(
        apk: File,
        packageName: String,
        version: String,
        label: String
    ) = withContext(Dispatchers.IO) {
        rootConnection.remoteFS?.let { remoteFS ->
            val assets = app.assets
            val modulePath = "$modulesPath/$packageName-revanced"

            remoteFS.getFile(modulePath).mkdir()

            "$modulePath/$packageName.apk".let { patchedAPK ->

                listOf(
                    assets.open("root/service.sh") to "service.sh",
                    assets.open("root/module.prop") to "module.prop",
                    remoteFS.getFile(apk.absolutePath).newInputStream() to packageName
                ).forEach { file ->
                    file.first.use { inputStream ->
                        remoteFS.getFile("$modulePath/$file").newOutputStream()
                            .use { outputStream ->
                                val buffer = ByteArray(1024)
                                var bytesRead: Int

                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    val content = String(buffer, 0, bytesRead)
                                        .replace("__PKG_NAME__", packageName)
                                        .replace("__VERSION__", version)
                                        .replace("__LABEL__", label)
                                        .toByteArray()

                                    outputStream.write(content)
                                }
                            }
                    }
                }

                remoteFS.getFile(apk.absolutePath).newInputStream().use { inputStream ->
                    remoteFS.getFile(patchedAPK).newOutputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Shell.cmd(
                    "chmod 644 $patchedAPK",
                    "chown system:system $patchedAPK",
                    "chcon u:object_r:apk_data_file:s0 $patchedAPK"
                ).exec()

            }
        }
    }

    fun uninstall(packageName: String) {
        rootConnection.remoteFS?.let { remoteFS ->
            if (isAppMounted(packageName))
                unmount(packageName)

            remoteFS.getFile("$modulesPath/$packageName-revanced").deleteRecursively()
        }
    }

    companion object {
        const val modulesPath = "/data/adb/modules"

        val isDeviceRooted =
            try {
                Runtime.getRuntime().exec("su --version")
                true
            } catch (_: IOException) {
                false
            }
    }
}