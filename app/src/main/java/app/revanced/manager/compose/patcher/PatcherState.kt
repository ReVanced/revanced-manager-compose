package app.revanced.manager.compose.patcher

import android.app.Application
import androidx.compose.runtime.mutableStateOf

class PatcherState(val app: Application) {
    private val bundle = mutableStateOf(PatchBundle(emptyList()))

    fun patchClassesFor(packageName: String, packageVersion: String) = bundle.value.getPatchesFiltered(packageName, packageVersion)
}