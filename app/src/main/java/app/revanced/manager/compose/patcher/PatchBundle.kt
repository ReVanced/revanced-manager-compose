package app.revanced.manager.compose.patcher

import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages

class PatchBundle(inner: Iterable<PatchClass>) {
    private companion object {
        const val allowExperimental = false
    }

    private val allPatches = inner.toList()

    /**
     * @return A list of patches that are compatible with this Apk.
     */
    fun getPatchesFiltered(packageName: String, packageVersion: String) = allPatches.filter { patch ->
        val compatiblePackages = patch.compatiblePackages
            ?: // The patch has no compatibility constraints, which means it is universal.
            return@filter true

        if (!compatiblePackages.any { it.name == packageName }) {
            // Patch is not compatible with this package.
            return@filter false
        }

        if (!(allowExperimental || compatiblePackages.any { it.versions.isEmpty() || it.versions.any { version -> version == packageVersion } })) {
            // Patch is not compatible with this version.
            return@filter false
        }

        true
    }



    fun getRecommendedVersion(packageName: String) = "0.69.420"
}