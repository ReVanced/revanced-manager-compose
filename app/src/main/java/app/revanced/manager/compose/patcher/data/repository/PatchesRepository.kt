package app.revanced.manager.compose.patcher.data.repository

import app.revanced.manager.compose.network.api.ManagerAPI
import app.revanced.manager.compose.patcher.data.PatchBundleDataSource

// TODO: this might make more sense in the "domain" layer.
class PatchesRepository(private val managerAPI: ManagerAPI) {
    private var bundle: PatchBundleDataSource? = null

    private suspend fun getBundle() = bundle ?: PatchBundleDataSource(
        managerAPI.downloadPatchBundle()!!.absolutePath,
        managerAPI.downloadIntegrations()
    ).also { bundle = it }

    suspend fun patchClassesFor(packageName: String, packageVersion: String) =
        getBundle().getPatchesFiltered(packageName, packageVersion)

    // TODO: move this out of here.
    suspend fun getIntegrations() = listOfNotNull(getBundle().integrations)
}