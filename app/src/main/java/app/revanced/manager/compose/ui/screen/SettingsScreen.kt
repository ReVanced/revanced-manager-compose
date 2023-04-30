package app.revanced.manager.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.revanced.manager.compose.R
import app.revanced.manager.compose.destination.Destination
import app.revanced.manager.compose.ui.component.settings.SettingsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    navigate: (Destination) -> Unit
) {
    val settingsSections = listOf(
        Triple(R.string.general_settings, R.string.general_settings, Outlined.Settings) to Destination.GeneralSettings,
        Triple(R.string.updates_settings, R.string.updates_settings, Outlined.Update) to Destination.UpdatesSettings,
        Triple(R.string.sources_settings, R.string.sources_settings, Outlined.SwapVert) to Destination.SourcesSettings,
        Triple(R.string.downloader_settings, R.string.downloader_settings, Outlined.Download) to Destination.DownloaderSettings,
        Triple(R.string.import_and_export_settings, R.string.import_export_settings_description, Outlined.SwapVert) to Destination.ImportExportSettings,
        Triple(R.string.about_settings, R.string.about_settings, Outlined.Info) to Destination.About,
    )

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize(),
        {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Outlined.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            settingsSections.forEach { (titleDescIcon, destination) ->
                SettingsSection(
                    title = stringResource(titleDescIcon.first),
                    description = stringResource(titleDescIcon.second),
                    icon = titleDescIcon.third,
                    onClick = { navigate(destination) }
                )
            }
        }
    }
}