package app.revanced.manager.compose.domain.manager.sources.impl

import app.revanced.manager.compose.domain.manager.sources.NetworkSource
import app.revanced.manager.compose.network.api.ManagerAPI
import java.io.File


class ReVancedAPISource(directory: File, private val api: ManagerAPI) : NetworkSource(directory) {
    private fun Pair<String, String>.toBundleVersion() =
        let { (patches, integrations) -> BundleVersion(patches, integrations) }

    override suspend fun downloadLatest(patches: File, integrations: File) =
        api.download(patches, integrations).toBundleVersion()

    override suspend fun getLatestVersion() = api.getLatestVersion().toBundleVersion()
}