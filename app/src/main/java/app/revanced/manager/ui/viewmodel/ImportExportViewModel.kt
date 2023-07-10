package app.revanced.manager.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.R
import app.revanced.manager.domain.manager.KeystoreManager
import app.revanced.manager.domain.repository.PatchSelectionRepository
import app.revanced.manager.domain.repository.SerializedSelection
import app.revanced.manager.domain.repository.SourceRepository
import app.revanced.manager.domain.sources.Source
import app.revanced.manager.util.JSON_MIMETYPE
import app.revanced.manager.util.toast
import app.revanced.manager.util.uiSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.deleteExisting

@OptIn(ExperimentalSerializationApi::class)
class ImportExportViewModel(
    private val app: Application,
    private val keystoreManager: KeystoreManager,
    private val selectionRepository: PatchSelectionRepository,
    sourceRepository: SourceRepository
) : ViewModel() {
    private val contentResolver = app.contentResolver
    val sources = sourceRepository.sources
    var selectedSource by mutableStateOf<Source?>(null)
        private set
    var selectionAction by mutableStateOf<SelectionAction?>(null)
        private set
    private var keystoreImportPath by mutableStateOf<Path?>(null)
    val showCredentialsDialog by derivedStateOf { keystoreImportPath != null }

    fun startKeystoreImport(content: Uri) = viewModelScope.launch {
        val path = withContext(Dispatchers.IO) {
            File.createTempFile("signing", "ks", app.cacheDir).toPath().also {
                Files.copy(
                    contentResolver.openInputStream(content)!!,
                    it,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }

        knownPasswords.forEach {
            if (tryKeystoreImport(KeystoreManager.DEFAULT, it, path)) {
                return@launch
            }
        }

        keystoreImportPath = path
    }

    fun cancelKeystoreImport() {
        keystoreImportPath?.deleteExisting()
        keystoreImportPath = null
    }

    suspend fun tryKeystoreImport(cn: String, pass: String) =
        tryKeystoreImport(cn, pass, keystoreImportPath!!)

    private suspend fun tryKeystoreImport(cn: String, pass: String, path: Path): Boolean {
        if (keystoreManager.import(cn, pass, path)) {
            cancelKeystoreImport()
            return true
        }

        return false
    }

    override fun onCleared() {
        super.onCleared()

        cancelKeystoreImport()
    }

    fun exportKeystore(target: Uri) = viewModelScope.launch {
        keystoreManager.export(contentResolver.openOutputStream(target)!!)
    }

    fun regenerateKeystore() = viewModelScope.launch {
        keystoreManager.regenerate()
        app.toast(app.getString(R.string.regenerate_keystore_success))
    }

    fun resetSelection() = viewModelScope.launch(Dispatchers.Default) {
        selectionRepository.reset()
    }

    fun executeSelectionAction(target: Uri) = viewModelScope.launch {
        val source = selectedSource!!
        val action = selectionAction!!
        clearSelectionAction()

        action.execute(source, target)
    }

    fun selectSource(source: Source) {
        selectedSource = source
    }

    fun clearSelectionAction() {
        selectionAction = null
        selectedSource = null
    }

    fun importSelection() = clearSelectionAction().also {
        selectionAction = Import()
    }

    fun exportSelection() = clearSelectionAction().also {
        selectionAction = Export()
    }

    sealed interface SelectionAction {
        suspend fun execute(source: Source, location: Uri)
        val activityContract: ActivityResultContract<String, Uri?>
        val activityArg: String
    }

    private inner class Import : SelectionAction {
        override val activityContract = ActivityResultContracts.GetContent()
        override val activityArg = JSON_MIMETYPE
        override suspend fun execute(source: Source, location: Uri) = uiSafe(
            app,
            R.string.restore_patches_selection_fail,
            "Failed to restore patches selection"
        ) {
            val selection = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(location)!!.use {
                    Json.decodeFromStream<SerializedSelection>(it)
                }
            }

            selectionRepository.import(source, selection)
        }
    }

    private inner class Export : SelectionAction {
        override val activityContract = ActivityResultContracts.CreateDocument(JSON_MIMETYPE)
        override val activityArg = "selection.json"
        override suspend fun execute(source: Source, location: Uri) = uiSafe(
            app,
            R.string.backup_patches_selection_fail,
            "Failed to backup patches selection"
        ) {
            val selection = selectionRepository.export(source)

            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(location, "wt")!!.use {
                    Json.Default.encodeToStream(selection, it)
                }
            }
        }
    }

    private companion object {
        val knownPasswords = setOf("ReVanced", "s3cur3p@ssw0rd")
    }
}