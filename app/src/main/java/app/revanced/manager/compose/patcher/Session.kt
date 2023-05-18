package app.revanced.manager.compose.patcher

import app.revanced.patcher.Patcher
import app.revanced.patcher.PatcherOptions
import app.revanced.patcher.logging.Logger
import android.util.Log
import app.revanced.patcher.data.Context
import app.revanced.patcher.patch.Patch
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal typealias PatchClass = Class<out Patch<Context>>
internal typealias PatchList = List<PatchClass>

class Session(
    cacheDir: String,
    frameworkDir: String,
    aaptPath: String,
    private val input: File,
    private val onProgress: suspend (Progress) -> Unit = { }
) : Closeable {
    enum class Progress {
        UNPACKING,
        MERGING,
        PATCHING,
        SAVING,
    }

    private val logger = LogcatLogger
    private val temporary = File(cacheDir).resolve("manager").also { it.mkdirs() }
    private val patcher = Patcher(
        PatcherOptions(
            inputFile = input,
            resourceCacheDirectory = temporary.resolve("aapt-resources").path,
            frameworkFolderLocation = frameworkDir,
            aaptPath = aaptPath,
            logger = logger,
        )
    )

    private companion object {
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

    suspend fun run(output: File, selectedPatches: PatchList, integrations: List<File>) {
        onProgress(Progress.MERGING)

        with(patcher) {
            logger.info("Merging integrations")
            addIntegrations(integrations) {}
            addPatches(selectedPatches)

            logger.info("Applying patches...")
            onProgress(Progress.PATCHING)

            applyPatchesVerbose()
        }

        onProgress(Progress.SAVING)
        logger.info("Writing patched files...")
        val result = patcher.save()

        val aligned = temporary.resolve("aligned.apk").also { Aligning.align(result, input, it) }

        val patched = if (shouldSign) sign(aligned) else aligned

        logger.info("Patched apk saved to $patched")

        Files.move(patched.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)
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