package app.revanced.manager.domain.repository

import app.revanced.manager.data.room.AppDatabase
import app.revanced.manager.data.room.apps.installed.AppliedPatch
import app.revanced.manager.data.room.apps.installed.InstallType
import app.revanced.manager.data.room.apps.installed.InstalledApp
import app.revanced.manager.util.PatchesSelection
import kotlinx.coroutines.flow.distinctUntilChanged

class InstalledAppRepository(
    db: AppDatabase
) {
    private val dao = db.installedAppDao()

    fun getAll() = dao.getAllApps().distinctUntilChanged()

    suspend fun get(packageName: String) = dao.get(packageName)

    suspend fun getAppliedPatches(packageName: String): PatchesSelection {
        return dao.getAppliedPatches(packageName)
            .groupBy { it.bundleUid }
            .mapValues { (_, appliedPatches) ->
                appliedPatches.map { it.patchName }.toSet()
            }
    }

    suspend fun add(
        currentPackageName: String,
        originalPackageName: String,
        version: String,
        installType: InstallType,
        patchesSelection: PatchesSelection
    ) {
        dao.insert(
            InstalledApp(
                currentPackageName = currentPackageName,
                originalPackageName = originalPackageName,
                version = version,
                installType = installType
            )
        )
        patchesSelection.forEach { (uid, patches) ->
            patches.forEach { patch ->
                dao.insertAppliedPatch(
                    AppliedPatch(
                        packageName = currentPackageName,
                        bundleUid = uid,
                        patchName = patch
                    )
                )
            }
        }
    }

    suspend fun delete(installedApp: InstalledApp) {
        dao.delete(installedApp)
    }
}