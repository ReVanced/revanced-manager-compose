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
import app.revanced.manager.network.dto.GithubChangelog
import app.revanced.manager.network.utils.getOrThrow
import app.revanced.manager.util.uiSafe
import kotlinx.coroutines.launch
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class ManagerUpdateChangelogViewModel(
    private val githubRepository: GithubRepository,
    private val app: Application,
) : ViewModel() {
    private val markdownFlavour = GFMFlavourDescriptor()
    private val markdownParser = MarkdownParser(flavour = markdownFlavour)

    var changelog by mutableStateOf(
        GithubChangelog(
            "...",
            app.getString(R.string.changelog_loading),
            emptyList()
        )
    )
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

    init {
        viewModelScope.launch {
            uiSafe(app, R.string.changelog_download_fail, "Failed to download changelog") {
                changelog = githubRepository.getChangelog("revanced-manager").getOrThrow()
            }
        }
    }
}
