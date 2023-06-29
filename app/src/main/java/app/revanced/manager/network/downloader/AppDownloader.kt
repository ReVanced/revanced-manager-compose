package app.revanced.manager.network.downloader

import app.revanced.manager.network.service.HttpService
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File

sealed class AppDownloader : KoinComponent {
    protected val client: HttpService = get()

    /**
     * Contains a list of versions with their download links
     */
    abstract val availableApps: StateFlow<Map<String, String>>

    abstract val downloadProgress: StateFlow<Float?>

    abstract val loadingText: StateFlow<String?>

    /**
     * Loads all available apps to [availableApps].
     *
     * @param apk The package name of the app.
     * @param versionFilter a set of versions to filter.
     */
    abstract suspend fun getAvailableVersionList(apk: String, versionFilter: Set<String>)

    /**
     * Downloads the specific app version
     *
     * @param link The download link from [availableApps].
     * @param savePath The folder where the downloaded app should be stored.
     * @param preferSplit whether it should prefer a split or a full apk.
     * @param preferUniversal whether it should prefer an universal or an arch-specific apk
     * @return the downloaded apk or the folder containing all split apks.
     */
    abstract suspend fun downloadApp(
        link: String,
        version: String,
        savePath: File,
        preferSplit: Boolean = false,
        preferUniversal: Boolean = false
    ): File
}