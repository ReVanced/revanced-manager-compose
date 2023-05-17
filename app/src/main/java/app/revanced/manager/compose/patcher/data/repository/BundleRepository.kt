package app.revanced.manager.compose.patcher.data.repository

import app.revanced.manager.compose.network.api.ManagerAPI
import app.revanced.manager.compose.patcher.data.PatchBundleDataSource

class BundleRepository(private val managerAPI: ManagerAPI) {
    private var bundle: PatchBundleDataSource? = null

    suspend fun getBundle() = bundle ?: PatchBundleDataSource(
        managerAPI.downloadPatchBundle()!!.absolutePath,
        managerAPI.downloadIntegrations()
    ).also { bundle = it }

}