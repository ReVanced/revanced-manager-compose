package app.revanced.manager.compose.ui.destination

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
    data class Installer(val input: Uri, val selectedPatches: List<String>) : Destination
}