package app.revanced.manager.network.downloader

import android.os.Build.SUPPORTED_ABIS
import app.revanced.manager.network.dto.APKMirrorResponse
import app.revanced.manager.network.utils.APIResponse
import app.revanced.manager.network.utils.getOrThrow
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import it.skrape.core.htmlDocument
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.form
import it.skrape.selects.html5.input
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class APKMirror : AppDownloader() {

    private val _availableApps: MutableStateFlow<Map<String, String>> = MutableStateFlow(emptyMap())
    override val availableApps = _availableApps.asStateFlow()

    private val _downloadProgress: MutableStateFlow<Float?> = MutableStateFlow(null)
    override val downloadProgress = _downloadProgress.asStateFlow()

    private val _loadingText: MutableStateFlow<String?> = MutableStateFlow(null)
    override val loadingText = _loadingText.asStateFlow()


    private suspend fun getAppInfo(packages: List<String>): APIResponse<APKMirrorResponse> =
        client.request {
            url("$apkMirror/wp-json/apkm/v1/app_exists/")
            method = HttpMethod.Post
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                append(HttpHeaders.Authorization, authorization)
            }
            contentType(ContentType.Application.Json)
            setBody(mapOf("pnames" to packages))
        }

    override suspend fun getAvailableVersionList(apk: String, versionFilter: Set<String>) {
        _availableApps.emit(emptyMap())
        _loadingText.emit("Checking if app is available...")
        val appInfo = getAppInfo(listOf(apk)).getOrThrow()

        if (appInfo.data.first().exists) {

            // APKMirror gives the wear os version of youtube music for some reason
            val appCategory = if (apk == "com.google.android.apps.youtube.music") "youtube-music" else appInfo.data.first().app!!.link.split("/")[3]
            val versions = mutableMapOf<String, String>()
            var page = 1

            _loadingText.emit("Loading available versions...")

            while (
                if (versionFilter.isNotEmpty())
                    versions.filterKeys { it in versionFilter }.size < versionFilter.size && page <= 7
                else
                    page <= 1
            ) {
                htmlDocument(
                    html = client.http.get {
                        url("$apkMirror/uploads/page/$page/")
                        parameter("appcategory", appCategory)
                    }.bodyAsText()
                ) {
                    div {
                        withClass = "widget_appmanager_recentpostswidget"
                        findFirst {
                            div {
                                withClass = "listWidget"
                                findFirst {
                                    children.forEach { element ->
                                        if (element.className.isEmpty()) {

                                            val version = element.div {
                                                withClass = "infoSlide"
                                                findFirst {
                                                    p {
                                                        findFirst {
                                                            span {
                                                                withClass = "infoSlide-value"
                                                                findFirst {
                                                                    text
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            val link = element.findFirst {
                                                a {
                                                    withClass = "downloadLink"
                                                    findFirst {
                                                        attribute("href")
                                                    }
                                                }
                                            }

                                            versions[version] = link
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                _availableApps.emit(
                    versions
                        .let {
                            if (versionFilter.isEmpty())
                                it
                            else
                                it.filterKeys { version -> versionFilter.contains(version) }
                        }
                        .toMap()
                )

                page += 1
            }

            _loadingText.emit(null)
        } else {
            throw Exception("App isn't available for download")
        }
    }

    override suspend fun downloadApp(
        link: String,
        version: String,
        savePath: File,
        preferSplit: Boolean,
        preferUniversal: Boolean
    ): File {
        _loadingText.emit("Loading variants...")
        _downloadProgress.emit(null)

        var isSplit = false

        val appPage = htmlDocument(
            html = client.http.get { url(apkMirror + link) }.bodyAsText()
        ) {
            div {
                withClass = "variants-table"
                findFirst { // list of variants

                    val supportedArches = SUPPORTED_ABIS.toMutableList().apply {
                        addAll(
                            if (preferUniversal) 0 else 1,
                            listOf("universal", "noarch")
                        )
                    }

                    val variants = children.drop(1)
                        .groupBy { if (it.text.contains("APK")) "full" else "split"}.let {

                        if (preferSplit)
                            it["split"]?.also { isSplit = true }
                                ?: it["full"]
                        else
                            it["full"]
                                ?: it["split"].also { isSplit = true }
                    }

                    if (isSplit) TODO("\nSplit apks are not supported yet")

                    supportedArches.firstNotNullOfOrNull { arch ->
                        variants?.find { it.text.contains(arch) }
                    }?.a {
                        findFirst {
                            attribute("href")
                        }
                    }

                }
            }
        }

        _loadingText.emit("Loading download page...")

        val downloadPage = htmlDocument(
            html = client.http.get { url(apkMirror + appPage) }.bodyAsText()
        ) {
            a {
                withClass = "downloadButton"
                findFirst {
                    attribute("href")
                }
            }
        }

        _loadingText.emit("Getting download link...")

        val downloadLink = htmlDocument(
            html = client.http.get { url(apkMirror + downloadPage) }.bodyAsText()
        ) {
            form {
                withId = "filedownload"
                findFirst {
                    val apkLink = attribute("action")
                    val id = input {
                        withAttribute = "name" to "id"
                        findFirst {
                            attribute("value")
                        }
                    }
                    val key = input {
                        withAttribute = "name" to "key"
                        findFirst {
                            attribute("value")
                        }
                    }
                    "$apkLink?id=$id&key=$key"
                }
            }
        }

        val saveLocation = if (isSplit)
            savePath.resolve(version).also { it.mkdirs() }
        else
            savePath.resolve("$version.apk")

        try {
            (if (isSplit)
                saveLocation.resolve("temp.zip")
            else
                saveLocation).let {

                client.download(saveLocation) {
                    url(apkMirror + downloadLink)
                    onDownload { bytesSentTotal, contentLength ->
                        _downloadProgress.emit(bytesSentTotal.toFloat() / contentLength.toFloat())
                        _loadingText.emit(
                            "Downloading apk... (${bytesSentTotal.toFloat().div(1000000)}/${contentLength.toFloat().div(1000000)})"
                        )
                    }
                }

            }

            if (isSplit) {
                // TODO: Extract temp.zip

                saveLocation.resolve("temp.zip").delete()
            }
        } catch (e: Exception) {
            saveLocation.deleteRecursively()
            throw e
        }
        _downloadProgress.emit(null)
        _loadingText.emit(null)

        return saveLocation
    }

    companion object {
        const val apkMirror = "https://www.apkmirror.com"

        const val authorization = "Basic YXBpLXRvb2xib3gtZm9yLWdvb2dsZS1wbGF5OkNiVVcgQVVMZyBNRVJXIHU4M3IgS0s0SCBEbmJL"
    }

}