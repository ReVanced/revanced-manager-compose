package app.revanced.manager.compose.patcher.data.repository

// TODO: this makes more sense in the "domain layer" as it is right now.
class PatchesRepository(private val bundleRepository: BundleRepository) {
    // private val bundle = PatchBundleDataSource(testingPatchBundle, null)

    suspend fun patchClassesFor(packageName: String, packageVersion: String) =
        bundleRepository.getBundle().getPatchesFiltered(packageName, packageVersion)

    // TODO: move this out of here.
    suspend fun getIntegrations() = listOfNotNull(bundleRepository.getBundle().integrations)
}