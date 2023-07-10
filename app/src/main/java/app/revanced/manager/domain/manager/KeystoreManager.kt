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
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

class KeystoreManager(private val app: Application, private val prefs: PreferencesManager) {
    companion object {
        /**
         * Default alias and password for the keystore.
         */
        const val DEFAULT = "ReVanced"
    }

    private val keystorePath =
        app.getDir("signing", Context.MODE_PRIVATE).resolve("manager.keystore").toPath()

    private suspend fun updatePrefs(cn: String, pass: String) = prefs.editor {
        prefs.keystoreCommonName.value = cn
        prefs.keystorePass.value = pass
    }

    suspend fun sign(input: File, output: File) = withContext(Dispatchers.Default) {
        Signer(
            SigningOptions(
                prefs.keystoreCommonName.get(),
                prefs.keystorePass.get(),
                keystorePath
            )
        ).signApk(
            input,
            output
        )
    }

    /*
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
     */

    suspend fun regenerate() = Signer(SigningOptions(DEFAULT, DEFAULT, keystorePath)).regenerateKeystore().also {
        updatePrefs(DEFAULT, DEFAULT)
    }

    suspend fun import(cn: String, pass: String, keystore: Path): Boolean {
        if (!Signer(SigningOptions(cn, pass, keystore)).canUnlock()) {
            return false
        }
        withContext(Dispatchers.IO) {
            Files.copy(keystore, keystorePath, StandardCopyOption.REPLACE_EXISTING)
        }

        updatePrefs(cn, pass)
        return true
    }

    suspend fun export(target: OutputStream) {
        withContext(Dispatchers.IO) {
            Files.copy(keystorePath, target)
        }
    }
}