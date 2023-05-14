package app.revanced.manager.compose.patcher.patch

import android.os.Parcelable
import app.revanced.manager.compose.patcher.PatchClass
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.extensions.PatchExtensions.dependencies
import app.revanced.patcher.extensions.PatchExtensions.description
import app.revanced.patcher.extensions.PatchExtensions.include
import app.revanced.patcher.extensions.PatchExtensions.patchName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PatchInfo(
    val name: String,
    val description: String?,
    val dependencies: List<String>?,
    val include: Boolean,
    val compatiblePackages: List<CompatiblePackage>?
) : Parcelable {
    constructor(patch: PatchClass) : this(
        patch.patchName,
        patch.description,
        patch.dependencies?.map { it.java.patchName },
        patch.include,
        patch.compatiblePackages?.map { CompatiblePackage(it) })
}

@Parcelize
data class CompatiblePackage(val name: String, val versions: List<String>) : Parcelable {
    constructor(pkg: Package) : this(pkg.name, pkg.versions.toList())
}