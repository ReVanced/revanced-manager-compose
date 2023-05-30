package app.revanced.manager.compose.domain.manager

import android.app.Application
import app.revanced.manager.compose.util.signing.Signer
import app.revanced.manager.compose.util.signing.SigningOptions
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
        const val defaultKeystoreValue = "ReVanced"
    }

    private val keystorePath = app.dataDir.resolve("manager.keystore").toPath()
    private fun options(cn: String, pass: String) = SigningOptions(cn, pass, keystorePath)

    fun createSigner() = Signer(options(prefs.keystoreCommonName!!, prefs.keystorePass!!))

    init {
        if (!keystorePath.exists()) {
            regenerate()
        }
    }

    fun regenerate() = createSigner().regenerateKeystore()

    fun import(cn: String, pass: String, keystore: InputStream) {
        val tempPath = app.cacheDir.resolve("keystore.tmp").toPath()
        Files.copy(keystore, tempPath)

        try {
            // TODO: check if the user actually provided the correct password
            Files.copy(tempPath, keystorePath, StandardCopyOption.REPLACE_EXISTING)

            prefs.keystoreCommonName = cn
            prefs.keystorePass = pass
        } finally {
            Files.delete(tempPath)
        }
    }

    fun export(target: OutputStream) {
        Files.copy(keystorePath, target)
    }
}