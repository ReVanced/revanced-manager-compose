package app.revanced.manager.compose.ui.component.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import app.revanced.manager.compose.domain.manager.PreferencesManager
import app.revanced.manager.compose.ui.theme.Theme
import kotlinx.collections.immutable.toImmutableMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettings(
    onBackClick: () -> Unit,
    preferences: PreferencesManager,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsHeader(title = stringResource(R.string.appearance_section))
            RadioSetting(
                label = stringResource(R.string.theme_setting),
                description = stringResource(R.string.theme_setting_description),
                value = preferences.theme,
                options = Theme.values().associateBy { theme -> stringResource(theme.displayName) }.toImmutableMap(),
                onConfirm = {preferences.theme = it }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SwitchSetting(
                    checked = preferences.dynamicColor,
                    text = stringResource(R.string.dynamic_color_setting),
                    description = stringResource(R.string.dynamic_color_setting_description),
                    onCheckedChange = { preferences.dynamicColor = it }
                )
            }
        }
    }
}

@Composable
fun UpdatesSettings(
    onBackClick: () -> Unit,
    preferences: PreferencesManager,
) {}

@Composable
fun SourcesSettings(
    onBackClick: () -> Unit,
    preferences: PreferencesManager,
) {}

@Composable
fun DownloaderSettings(
    onBackClick: () -> Unit,
    preferences: PreferencesManager,
) {}

@Composable
fun ImportExportSettings(
    onBackClick: () -> Unit,
    preferences: PreferencesManager,
) {}

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    preferences: PreferencesManager,
) {}