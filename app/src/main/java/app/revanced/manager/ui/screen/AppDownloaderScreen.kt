package app.revanced.manager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.LoadingIndicator
import app.revanced.manager.ui.viewmodel.AppDownloaderViewModel
import app.revanced.manager.util.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDownloaderScreen(
    onBackClick: () -> Unit,
    onApkClick: (AppInfo) -> Unit,
    viewModel: AppDownloaderViewModel
) {
    val downloadProgress by viewModel.appDownloader.downloadProgress.collectAsStateWithLifecycle()
    val loadingText by viewModel.appDownloader.loadingText.collectAsStateWithLifecycle()
    val availableVersions by viewModel.appDownloader.availableApps.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.select_version),
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.HelpOutline, stringResource(R.string.help))
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Search, stringResource(R.string.search))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.errorMessage == null) {
                if (viewModel.isDownloading == null) {
                    if (availableVersions.isNotEmpty()) {


                        availableVersions.forEach { (version, link) ->
                            ListItem(
                                modifier = Modifier.clickable(enabled = viewModel.isDownloading == null) {
                                    viewModel.downloadApp(
                                        link,
                                        onComplete = onApkClick
                                    )
                                },
                                headlineContent = { Text(version) },
                                trailingContent = viewModel.compatibleVersions[version]?.let {
                                    {
                                        Text(
                                            pluralStringResource(
                                                R.plurals.patches_count,
                                                count = it,
                                                it
                                            )
                                        )
                                    }
                                }
                            )
                        }
                        if (viewModel.isLoading)
                            LoadingIndicator()


                    } else {
                        LoadingIndicator(
                            text = loadingText
                        )
                    }
                } else {
                    LoadingIndicator(
                        progress = downloadProgress,
                        text = loadingText
                    )
                }
            } else {
                Text("An error occurred:")
                Text(
                    text = viewModel.errorMessage!!,
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }
        }
    }
}