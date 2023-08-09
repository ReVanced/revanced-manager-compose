package app.revanced.manager.domain.repository

import app.revanced.manager.data.room.AppDatabase
import app.revanced.manager.data.room.apps.installed.InstallType
import app.revanced.manager.data.room.apps.installed.InstalledApp
import kotlinx.coroutines.flow.distinctUntilChanged

class InstalledAppRepository(
    db: AppDatabase
) {
    private val dao = db.installedAppDao()

    fun getAll() = dao.getAllApps().distinctUntilChanged()

    suspend fun get(packageName: String) = dao.get(packageName)

    suspend fun add(
        currentPackageName: String,
        originalPackageName: String,
        version: String,
        installType: InstallType
    ) = dao.insert(
        InstalledApp(
            currentPackageName = currentPackageName,
            originalPackageName = originalPackageName,
            version = version,
            installType = installType
        )
    )

    suspend fun delete(installedApp: InstalledApp) {
        dao.delete(installedApp)
    }
}