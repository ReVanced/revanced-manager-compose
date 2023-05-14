package app.revanced.manager.compose.patcher

import app.revanced.patcher.Patcher
import app.revanced.patcher.PatcherOptions
import app.revanced.patcher.logging.Logger
import android.util.Log
import app.revanced.patcher.data.Context
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.patch.Patch
import java.io.Closeable
import java.io.File
import java.nio.file.Files

internal typealias PatchClass = Class<out Patch<Context>>
internal typealias PatchList = List<PatchClass>

class Session(cacheDir: String, private val input: File, patchBundle: Iterable<PatchClass>) : Closeable {
    private val logger = LogcatLogger
    private val patcher = Patcher(
        PatcherOptions(
            inputFile = input,
            resourceCacheDirectory = cacheDir,
            aaptPath = null,
            logger = logger,
        )
    )

    private val allPatches = patchBundle.toList()

    private val temporary = File(cacheDir).resolve("manager").also { it.mkdirs() }

    private companion object {
        const val allowExperimental = false

        const val shouldSign = false
    }

    private fun Patcher.applyPatchesVerbose() {
        this.executePatches().forEach { (patch, result) ->
            if (result.isSuccess) {
                logger.info("$patch succeeded")
                return@forEach
            }
            logger.error("$patch failed:")
            result.exceptionOrNull()!!.printStackTrace()
        }
    }

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

    fun run(output: File, selectedPatches: PatchList) {
        with(patcher) {
            logger.info("Merging integrations")
            addIntegrations(emptyList()) {} // TODO: actually add integrations

            addPatches(selectedPatches)

            logger.info("Applying patches...")
            applyPatchesVerbose()
        }

        logger.info("Writing patched files...")
        val result = patcher.save()

        val aligned = temporary.resolve("aligned.apk").also { Aligning.align(result, input, it) }

        val patched = if (shouldSign) sign(aligned) else aligned

        logger.info("Patched apk saved to $patched")

        Files.copy(patched.toPath(), output.toPath())
    }

    private fun sign(aligned: File): File = TODO()

    override fun close() {
        temporary.delete()
    }
}

private object LogcatLogger : Logger {
    private const val tag = "revanced-patcher"
    override fun error(msg: String) {
        Log.e(tag, msg)
    }

    override fun warn(msg: String) {
        Log.w(tag, msg)
    }

    override fun info(msg: String) {
        Log.i(tag, msg)
    }

    override fun trace(msg: String) {
        Log.v(tag, msg)
    }
}