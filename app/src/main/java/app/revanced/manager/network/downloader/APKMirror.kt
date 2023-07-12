package app.revanced.manager.network.downloader

import android.os.Build.SUPPORTED_ABIS
import app.revanced.manager.network.service.HttpService
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
import kotlinx.coroutines.flow.flow
import java.io.File

class APKMirror(
    private val client: HttpService
) : AppDownloader {
    private val _downloadProgress: MutableStateFlow<Pair<Float, Float>?> = MutableStateFlow(null)
    override val downloadProgress = _downloadProgress.asStateFlow()

    private val versionMap = HashMap<String, String>()

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

    override fun getAvailableVersions(packageName: String, versionFilter: Set<String>) = flow {

        // Vanced music uses the same package name so we have to hardcode...
        val appCategory = if (packageName == "com.google.android.apps.youtube.music")
            "youtube-music"
        else
            getAppLink(packageName).split("/")[3]

        var page = 1

        while (
            if (versionFilter.isNotEmpty())
                versionMap.filterKeys { it in versionFilter }.size < versionFilter.size && page <= 7
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
                            children.mapNotNull { element ->
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

                                    versionMap[version] = link
                                    version
                                } else null
                            }
                        }
                    }
                }
            }.onEach { versions -> emit(versions) }

            page++
        }
    }

    override suspend fun downloadApp(
        version: String,
        saveDirectory: File,
        preferSplit: Boolean,
        preferUniversal: Boolean
    ): File {
        _downloadProgress.emit(null)

        var isSplit = false

        val appPage = client.getHtml { url(apkMirror + versionMap[version]) }
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
            saveDirectory.resolve(version).also { it.mkdirs() }
        else
            saveDirectory.resolve("$version.apk")

        try {
            val downloadLocation = if (isSplit)
                saveLocation.resolve("temp.zip")
            else
                saveLocation

            client.download(downloadLocation) {
                url(apkMirror + downloadLink)
                onDownload { bytesSentTotal, contentLength ->
                    _downloadProgress.emit(bytesSentTotal.div(100000).toFloat().div(10) to contentLength.div(100000).toFloat().div(10))
                }
            }

            if (isSplit) {
                // TODO: Extract temp.zip

                downloadLocation.delete()
            }
        } catch (e: Exception) {
            saveLocation.deleteRecursively()
            throw e
        } finally {
            _downloadProgress.emit(null)
        }

        return saveLocation
    }

    companion object {
        const val apkMirror = "https://www.apkmirror.com"
    }

}