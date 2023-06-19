package app.revanced.manager.ui.viewmodel


import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.R
import app.revanced.manager.domain.manager.KeystoreManager
import app.revanced.manager.domain.repository.PatchSelectionRepository
import app.revanced.manager.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImportExportViewModel(private val app: Application, private val keystoreManager: KeystoreManager, private val selectionRepository: PatchSelectionRepository) : ViewModel() {
    private val contentResolver = app.contentResolver

    fun importKeystore(content: Uri, cn: String, pass: String) =
        keystoreManager.import(cn, pass, contentResolver.openInputStream(content)!!)

    fun exportKeystore(target: Uri) = keystoreManager.export(contentResolver.openOutputStream(target)!!)

    fun regenerateKeystore() = keystoreManager.regenerate().also {
        app.toast(app.getString(R.string.regenerate_keystore_success))
    }

    fun resetSelection() = viewModelScope.launch(Dispatchers.Default) {
        selectionRepository.reset()
    }
}