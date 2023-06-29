package app.revanced.manager.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.GroupHeader
import app.revanced.manager.ui.viewmodel.DownloadsViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: DownloadsViewModel = getViewModel()
) {
    val prefs = viewModel.prefs

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.downloads),
                onBackClick = onBackClick,
                actions = {
                    if (viewModel.selection.isNotEmpty()) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                modifier = Modifier.clickable { prefs.preferSplits = !prefs.preferSplits },
                headlineContent = { Text("Prefer split apks") },
                supportingContent = { Text("Prefer split apks instead of full apks") },
                trailingContent = {
                    Switch(checked = prefs.preferSplits, onCheckedChange = { prefs.preferSplits = it })
                }
            )

            ListItem(
                modifier = Modifier.clickable { prefs.preferUniversal = !prefs.preferUniversal },
                headlineContent = { Text("Prefer universal apks") },
                supportingContent = { Text("Prefer universal instead of arch-specific apks") },
                trailingContent = {
                    Switch(checked = prefs.preferUniversal, onCheckedChange = { prefs.preferUniversal = it })
                }
            )

            GroupHeader(title = "Downloaded apps")

            viewModel.downloadedApps.forEach {
                ListItem(
                    modifier = Modifier.clickable { viewModel.toggleItem(it) },
                    headlineContent = { Text(it.packageName) },
                    supportingContent = { Text(it.version) },
                    tonalElevation = if (viewModel.selection.contains(it)) 8.dp else 0.dp
                )
            }
        }
    }
}