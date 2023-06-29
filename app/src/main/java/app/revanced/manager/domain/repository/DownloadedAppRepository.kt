package app.revanced.manager.domain.repository

import app.revanced.manager.data.room.AppDatabase
import app.revanced.manager.data.room.apps.DownloadedApp
import java.io.File

class DownloadedAppRepository(
    db: AppDatabase
) {
    private val dao = db.appDao()

    suspend fun getAll() = dao.getAllApps()

    suspend fun get(packageName: String, version: String) = dao.get(packageName, version)

    suspend fun add(
        packageName: String,
        version: String,
        file: File
    ) = dao.insert(
        DownloadedApp(
            packageName = packageName,
            version = version,
            file = file
        )
    )

    suspend fun delete(downloadedApp: DownloadedApp) {
        downloadedApp.file.deleteRecursively()

        dao.delete(downloadedApp)
    }
}