package app.revanced.manager.ui.destination

import android.os.Parcelable
import app.revanced.manager.util.AppMeta
import app.revanced.manager.util.Options
import app.revanced.manager.util.PatchesSelection
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

sealed interface Destination : Parcelable {

    @Parcelize
    object Dashboard : Destination

    @Parcelize
    object AppSelector : Destination

    @Parcelize
    object Settings : Destination

    @Parcelize
    data class AppDownloader(val app: AppMeta) : Destination

    @Parcelize
    data class PatchesSelector(val input: AppMeta) : Destination

    @Parcelize
    data class Installer(val app: AppMeta, val selectedPatches: PatchesSelection, val options: @RawValue Options) : Destination
}