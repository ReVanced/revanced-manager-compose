package app.revanced.manager.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.R
import app.revanced.manager.domain.repository.GithubRepository
import app.revanced.manager.network.api.ManagerAPI
import app.revanced.manager.network.dto.GithubChangelog
import app.revanced.manager.network.utils.getOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.revanced.manager.util.PM
import app.revanced.manager.util.uiSafe
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class UpdateSettingsViewModel(
    private val managerAPI: ManagerAPI,
    private val githubRepository: GithubRepository,
    private val app: Application,
    private val pm: PM
) : ViewModel() {
    private val markdownFlavour = GFMFlavourDescriptor()
    private val markdownParser = MarkdownParser(flavour = markdownFlavour)

    val downloadProgress get() = (managerAPI.downloadProgress?.times(100)) ?: 0f
    val downloadedSize get() = managerAPI.downloadedSize ?: 0L
    val totalSize get() = managerAPI.totalSize ?: 0L
    val isInstalling by derivedStateOf { downloadProgress >= 100 }
    var changelog by mutableStateOf(GithubChangelog("0","Loading changelog", emptyList()))
        private set
    val formattedDownloadCount by derivedStateOf {
        val downloadCount = changelog.assets.firstOrNull()?.downloadCount?.toDouble() ?: 0.0
        if (downloadCount > 1000) {
            val roundedValue =
                (downloadCount / 100).toInt() / 10.0 // Divide by 100 and round to one decimal place
            "${roundedValue}k"
        } else {
            downloadCount.toString()
        }
    }
    val changelogHtml by derivedStateOf {
        val markdown = changelog.body
        val parsedTree = markdownParser.buildMarkdownTreeFromString(markdown)
        HtmlGenerator(markdown, parsedTree, markdownFlavour).generateHtml()
    }

    private val location = app.cacheDir.resolve("revanced-manager.apk")

    fun downloadLatestManager() {
        viewModelScope.launch(Dispatchers.IO) {
            managerAPI.downloadManager(location)
        }
    }

    init {
        downloadLatestManager()
        getChangelog()
    }
     fun getChangelog() {
        viewModelScope.launch {
            uiSafe(app, R.string.changelog_download_fail, "Failed to download changelog") {
                changelog = githubRepository.getChangelog("revanced-manager").getOrThrow()
            }
        }
    }
    fun installUpdate() {
        pm.installApp(listOf(location))
    }

    override fun onCleared() {
        super.onCleared()

        location.delete()
    }
}
