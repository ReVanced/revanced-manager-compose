package app.revanced.manager.domain.manager

import android.app.Application
import android.content.Context
import app.revanced.manager.util.signing.Signer
import app.revanced.manager.util.signing.SigningOptions
import app.revanced.manager.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

class KeystoreManager(private val app: Application, private val prefs: PreferencesManager) {
    companion object {
        /**
         * Default common name and password for the keystore.
         */
        const val DEFAULT = "ReVanced"

        /**
         * The default password used by the Flutter version.
         */
        const val FLUTTER_MANAGER_PASSWORD = "s3cur3p@ssw0rd"
    }

    private val keystorePath =
        app.getDir("signing", Context.MODE_PRIVATE).resolve("manager.keystore").toPath()

    private fun options(cn: String, pass: String) = SigningOptions(cn, pass, keystorePath)

    private suspend fun updatePrefs(cn: String, pass: String) = prefs.editor {
        prefs.keystoreCommonName.value = cn
        prefs.keystorePass.value = pass
    }

    suspend fun sign(input: File, output: File) = withContext(Dispatchers.Default) {
        Signer(options(prefs.keystoreCommonName.get(), prefs.keystorePass.get())).signApk(
            input,
            output
        )
    }

    init {
        if (!keystorePath.exists()) {
            runBlocking {
                initialize()
            }
        }
    }

    private suspend fun initialize() = try {
        withTimeout(1500L) {
            regenerate()
        }
    } catch (_: TimeoutCancellationException) {
        app.toast("Failed to generate keystore quickly enough!")
    }

    suspend fun regenerate() = Signer(options(DEFAULT, DEFAULT)).regenerateKeystore().also {
        updatePrefs(DEFAULT, DEFAULT)
    }

    suspend fun import(cn: String, pass: String, keystore: InputStream) {
        // TODO: check if the user actually provided the correct password
        withContext(Dispatchers.IO) {
            Files.copy(keystore, keystorePath, StandardCopyOption.REPLACE_EXISTING)
        }

        updatePrefs(cn, pass)
    }

    fun export(target: OutputStream) {
        Files.copy(keystorePath, target)
    }
}