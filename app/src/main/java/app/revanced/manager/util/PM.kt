package app.revanced.manager.util

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.os.Build
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import app.revanced.manager.domain.repository.SourceRepository
import app.revanced.manager.service.InstallService
import app.revanced.manager.service.UninstallService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.io.File

private const val byteArraySize = 1024 * 1024 // Because 1,048,576 is not readable

@Immutable
@Parcelize
data class AppMeta(
    val packageName: String,
    val versionName: String,
    val patches: Int,
    val path: File? = null
) : Parcelable

data class PackageMeta(val app: AppMeta, val packageInfo: PackageInfo?)

@SuppressLint("QueryPermissionsNeeded")
@Suppress("DEPRECATION")
class PM(
    private val app: Application,
    private val sourceRepository: SourceRepository
) {
    private val installedApps = MutableStateFlow(emptyList<PackageMeta>())
    private val compatibleApps = MutableStateFlow(emptyList<PackageMeta>())

    val appList: Flow<List<PackageMeta>> =
        compatibleApps.combine(installedApps) { compatibleApps, installedApps ->
            if (compatibleApps.isNotEmpty()) {
                (compatibleApps + installedApps)
                    .distinctBy { it.app.packageName }
                    .sortedWith(
                        compareByDescending<PackageMeta> {
                            it.app.patches
                        }.thenBy {
                            it.packageInfo?.applicationInfo?.loadLabel(app.packageManager)
                                ?.toString()
                        }.thenBy { it.app.packageName }
                    )
            } else {
                emptyList()
            }
        }

    suspend fun getCompatibleApps() {
        sourceRepository.bundles.collect { bundles ->
            val compatiblePackages = HashMap<String, Int>()

            bundles.flatMap { it.value.patches }.forEach {
                it.compatiblePackages?.forEach { pkg ->
                    compatiblePackages[pkg.name] = compatiblePackages.getOrDefault(pkg.name, 0) + 1
                }
            }

            withContext(Dispatchers.IO) {
                compatibleApps.emit(
                    compatiblePackages.keys.map { pkg ->
                        try {
                            val packageInfo = app.packageManager.getPackageInfo(pkg, 0)
                            PackageMeta(
                                AppMeta(
                                    pkg,
                                    packageInfo.versionName,
                                    compatiblePackages[pkg] ?: 0,
                                    File(packageInfo.applicationInfo.sourceDir)
                                ),
                                packageInfo
                            )
                        } catch (e: PackageManager.NameNotFoundException) {
                            PackageMeta(
                                AppMeta(
                                    pkg,
                                    "",
                                    compatiblePackages[pkg] ?: 0,
                                    null
                                ), null
                            )
                        }
                    }
                )
            }
        }
    }

    suspend fun getInstalledApps() {
        installedApps.emit(
            app.packageManager.getInstalledPackages(MATCH_UNINSTALLED_PACKAGES).map { packageInfo ->
                PackageMeta(
                    AppMeta(
                        packageInfo.packageName,
                        packageInfo.versionName,
                        0,
                        File(packageInfo.applicationInfo.sourceDir)
                    ),
                    packageInfo
                )
            })
    }

    suspend fun installApp(apks: List<File>) = withContext(Dispatchers.IO) {
        val packageInstaller = app.packageManager.packageInstaller
        packageInstaller.openSession(packageInstaller.createSession(sessionParams)).use { session ->
            apks.forEach { apk -> session.writeApk(apk) }
            session.commit(app.installIntentSender)
        }
    }

    fun uninstallPackage(pkg: String) {
        val packageInstaller = app.packageManager.packageInstaller
        packageInstaller.uninstall(pkg, app.uninstallIntentSender)
    }

    fun launch(pkg: String) = app.packageManager.getLaunchIntentForPackage(pkg)?.let {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        app.startActivity(it)
    }

    fun getApkInfo(apk: File) = app.packageManager.getPackageArchiveInfo(apk.path, 0)?.let {
        AppMeta(
            it.packageName,
            it.versionName,
            0,
            apk
        )
    }
}

private fun PackageInstaller.Session.writeApk(apk: File) {
    apk.inputStream().use { inputStream ->
        openWrite(apk.name, 0, apk.length()).use { outputStream ->
            inputStream.copyTo(outputStream, byteArraySize)
            fsync(outputStream)
        }
    }
}

private val intentFlags
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        PendingIntent.FLAG_MUTABLE
    else
        0

private val sessionParams
    get() = PackageInstaller.SessionParams(
        PackageInstaller.SessionParams.MODE_FULL_INSTALL
    ).apply {
        setInstallReason(PackageManager.INSTALL_REASON_USER)
    }

private val Context.installIntentSender
    get() = PendingIntent.getService(
        this,
        0,
        Intent(this, InstallService::class.java),
        intentFlags
    ).intentSender

private val Context.uninstallIntentSender
    get() = PendingIntent.getService(
        this,
        0,
        Intent(this, UninstallService::class.java),
        intentFlags
    ).intentSender