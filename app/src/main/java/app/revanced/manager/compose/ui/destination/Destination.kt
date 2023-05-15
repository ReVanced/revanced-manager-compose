package app.revanced.manager.compose.ui.destination

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

sealed interface Destination: Parcelable {

    @Parcelize
    object Dashboard: Destination

    @Parcelize
    object AppSelector: Destination

    @Parcelize
    object Settings: Destination

    @Parcelize
    object PatchesSelector: Destination

    @Parcelize
    data class Patching(val input: Uri, val selectedPatches: List<String>) : Destination
}