package app.revanced.manager.domain.repository

import app.revanced.manager.network.api.MissingAssetException
import app.revanced.manager.network.dto.Asset
import app.revanced.manager.network.dto.ReVancedReleases
import app.revanced.manager.network.service.ReVancedService
import app.revanced.manager.network.utils.getOrThrow
import app.revanced.manager.util.apiURL

class ReVancedRepository(
    private val service: ReVancedService
) {
    suspend fun getContributors() = service.getContributors(apiURL)

    suspend fun getAssets(api: String = apiURL) = Assets(service.getAssets(api).getOrThrow())
}

class Assets(private val releases: ReVancedReleases): List<Asset> by releases.tools {
    fun find(repo: String, file: String) = find { asset ->
        asset.name.contains(file) && asset.repository.contains(repo)
    } ?: throw MissingAssetException()
}