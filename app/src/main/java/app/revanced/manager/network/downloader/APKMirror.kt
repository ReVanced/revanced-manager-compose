package app.revanced.manager.network.downloader

import android.os.Build.SUPPORTED_ABIS
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.form
import it.skrape.selects.html5.h5
import it.skrape.selects.html5.input
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class APKMirror : AppDownloader() {

    private val _availableApps: MutableStateFlow<Map<String, String>> = MutableStateFlow(emptyMap())
    override val availableApps = _availableApps.asStateFlow()

    private val _downloadProgress: MutableStateFlow<Pair<Float, Float>?> = MutableStateFlow(null)
    override val downloadProgress = _downloadProgress.asStateFlow()

    private suspend fun getAppLink(packageName: String): String {
        val searchResults = client.getHtml { url("$apkMirror/?post_type=app_release&searchtype=app&s=$packageName") }
            .div {
                withId = "content"
                findFirst {
                    div {
                        withClass = "listWidget"
                        findAll {

                            find {
                                it.children.first().text.contains(packageName)
                            }!!.children.mapNotNull {
                                if (it.classNames.isEmpty()) {
                                    it.h5 {
                                        withClass = "appRowTitle"
                                        findFirst {
                                            a {
                                                findFirst {
                                                    attribute("href")
                                                }
                                            }
                                        }
                                    }
                                } else null
                            }

                        }
                    }
                }
            }

        return searchResults.find { url ->
            client.getHtml { url(apkMirror + url) }
                .div {
                    withId = "primary"
                    findFirst {
                        div {
                            withClass = "tab-buttons"
                            findFirst {
                                div {
                                    withClass = "tab-button-positioning"
                                    findFirst {
                                        children.any {
                                            it.attribute("href") == "https://play.google.com/store/apps/details?id=$packageName"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        } ?: throw Exception("App isn't available for download")
    }

    override suspend fun getAvailableVersionList(packageName: String, versionFilter: Set<String>) {
        _availableApps.emit(emptyMap())

        // Vanced music uses the same package name so we have to hardcode...
        val appCategory = if (packageName == "com.google.android.apps.youtube.music")
            "youtube-music"
        else
            getAppLink(packageName).split("/")[3]

        val versions = HashMap<String, String>()
        var page = 1

        while (
            if (versionFilter.isNotEmpty())
                versions.filterKeys { it in versionFilter }.size < versionFilter.size && page <= 7
            else
                page <= 1
        ) {
            client.getHtml {
                url("$apkMirror/uploads/page/$page/")
                parameter("appcategory", appCategory)
            }.div {
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
    }

    override suspend fun downloadApp(
        link: String,
        version: String,
        savePath: File,
        preferSplit: Boolean,
        preferUniversal: Boolean
    ): File {
        _downloadProgress.emit(null)

        var isSplit = false

        val appPage = client.getHtml { url(apkMirror + link) }
            .div {
                withClass = "variants-table"
                findFirst { // list of variants

                    val supportedArches = SUPPORTED_ABIS.toMutableList().apply {
                        addAll(
                            index = if (preferUniversal) 0 else 1,
                            listOf("universal", "noarch")
                        )
                    }

                    val variants = children.drop(1)
                        .groupBy { if (it.text.contains("APK")) "full" else "split" }.let {

                            if (preferSplit)
                                it["split"]?.also { isSplit = true }
                                    ?: it["full"]
                            else
                                it["full"]
                                    ?: it["split"].also { isSplit = true }
                        } ?: throw Exception("No variants, this should never happen")

                    if (isSplit) TODO("\nSplit apks are not supported yet")

                    supportedArches.firstNotNullOfOrNull { arch ->
                        variants.find { it.text.contains(arch) }
                    }?.a {
                        findFirst {
                            attribute("href")
                        }
                    } ?: throw Exception("No compatible variant found")

                }
            }

        val downloadPage = client.getHtml { url(apkMirror + appPage) }
            .a {
                withClass = "downloadButton"
                findFirst {
                    attribute("href")
                }
            }

        val downloadLink = client.getHtml { url(apkMirror + downloadPage) }
            .form {
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
                        _downloadProgress.emit(bytesSentTotal.div(100000).toFloat().div(10) to contentLength.div(100000).toFloat().div(10))
                    }
                }

            }

            if (isSplit) {
                // TODO: Extract temp.zip

                saveLocation.resolve("temp.zip").delete()
            }
        } catch (e: Exception) {
            saveLocation.deleteRecursively()
            _downloadProgress.emit(null)
            throw e
        }
        _downloadProgress.emit(null)

        return saveLocation
    }

    companion object {
        const val apkMirror = "https://www.apkmirror.com"
    }

}