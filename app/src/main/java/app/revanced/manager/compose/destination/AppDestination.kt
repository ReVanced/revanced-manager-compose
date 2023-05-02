package app.revanced.manager.compose.destination

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Destination: Parcelable {

    @Parcelize
    object Dashboard: Destination

    @Parcelize
    object Settings : Destination

    @Parcelize
    object GeneralSettings: Destination

    @Parcelize
    object UpdatesSettings: Destination

    @Parcelize
    object SourcesSettings: Destination

    @Parcelize
    object DownloaderSettings: Destination

    @Parcelize
    object ImportExportSettings: Destination

    @Parcelize
    object About: Destination

}