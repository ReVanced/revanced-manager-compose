package app.revanced.manager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.GroupHeader
import app.revanced.manager.ui.component.LoadingIndicator
import app.revanced.manager.ui.model.SelectedApp
import app.revanced.manager.ui.viewmodel.VersionSelectorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionSelectorScreen(
    onBackClick: () -> Unit,
    onAppClick: (SelectedApp) -> Unit,
    viewModel: VersionSelectorViewModel
) {
    val supportedVersions by viewModel.supportedVersions.collectAsStateWithLifecycle(emptyMap())
    val downloadedVersions by viewModel.downloadedVersions.collectAsStateWithLifecycle(emptyList())

    val list by remember {
        derivedStateOf {
            (downloadedVersions + viewModel.downloadableVersions)
                .distinctBy { it.version }
                .sortedWith(
                    compareByDescending<SelectedApp> {
                        it is SelectedApp.Local
                    }.thenByDescending { supportedVersions[it.version] }
                        .thenByDescending { it.version }
                )
        }
    }

    var selectedVersion: SelectedApp? by rememberSaveable { mutableStateOf(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.select_version),
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.HelpOutline, stringResource(R.string.help))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Select version") },
                icon = { Icon(Icons.Default.Check, null) },
                onClick = { selectedVersion?.let(onAppClick) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            viewModel.installedApp?.let { (packageInfo, alreadyPatched) ->
                SelectedApp.Installed(
                    packageName = viewModel.packageName,
                    version = packageInfo.versionName
                ).let {
                    SelectedAppItem(
                        selectedApp = it,
                        selected = selectedVersion == it,
                        onClick = { selectedVersion = it },
                        patchCount = supportedVersions[it.version],
                        alreadyPatched = alreadyPatched
                    )
                }
            }

            GroupHeader("Downloadable versions")

            list.forEach {
                SelectedAppItem(
                    selectedApp = it,
                    selected = selectedVersion == it,
                    onClick = { selectedVersion = it },
                    patchCount = supportedVersions[it.version]
                )
            }

            if (viewModel.errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.error_occurred))
                    Text(
                        text = viewModel.errorMessage!!,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                }
            } else if (viewModel.isLoading)
                LoadingIndicator()

        }
    }
}

@Composable
fun SelectedAppItem(
    selectedApp: SelectedApp,
    selected: Boolean,
    onClick: () -> Unit,
    patchCount: Int?,
    alreadyPatched: Boolean = false
) {
    ListItem(
        leadingContent = { RadioButton(selected, null) },
        headlineContent = { Text(selectedApp.version) },
        supportingContent = when (selectedApp) {
            is SelectedApp.Installed ->
                if (alreadyPatched) {
                    { Text("Already patched") }
                } else {
                    { Text("Installed") }
                }

            is SelectedApp.Local -> {
                { Text(stringResource(R.string.already_downloaded)) }
            }

            else -> null
        },
        trailingContent = patchCount?.let { {
            Text(pluralStringResource(R.plurals.patches_count, it, it))
        } },
        modifier = Modifier
            .clickable(enabled = !alreadyPatched, onClick = onClick)
            .run {
                if (alreadyPatched) alpha(0.5f)
                else this
            }
    )
}