package app.revanced.manager.network.downloader

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface AppDownloader {

    /**
     * Contains a list of versions with their download links.
     */
    val availableApps: StateFlow<Map<String, String>>

    val downloadProgress: StateFlow<Pair<Float, Float>?>

    /**
     * Loads all available apps to [availableApps].
     *
     * @param packageName The package name of the app.
     * @param versionFilter A set of versions to filter.
     */
    suspend fun getAvailableVersionList(packageName: String, versionFilter: Set<String>)

    /**
     * Downloads the specific app version.
     *
     * @param link The download link from [availableApps].
     * @param savePath The folder where the downloaded app should be stored.
     * @param preferSplit Whether it should prefer a split or a full apk.
     * @param preferUniversal Whether it should prefer an universal or an arch-specific apk.
     * @return the downloaded apk or the folder containing all split apks.
     */
    suspend fun downloadApp(
        link: String,
        version: String,
        savePath: File,
        preferSplit: Boolean = false,
        preferUniversal: Boolean = false
    ): File
}