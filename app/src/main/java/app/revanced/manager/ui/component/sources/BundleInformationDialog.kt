package app.revanced.manager.ui.component.sources

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.revanced.manager.R
import app.revanced.manager.domain.sources.Source
import app.revanced.manager.ui.component.BundleTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BundleInformationDialog(
    onDismissRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    onBackIcon: @Composable () -> Unit,
    topBarTitle: String,
    source: Source,
    remoteName: String = "",
    patchCount: Int = 0,
) {
    var checked by remember { mutableStateOf(true) }
    var viewCurrentBundlePatches by remember { mutableStateOf(false) }

    val patchInfoText = if (patchCount == 0) "No Patches available to view"
    else "$patchCount Patches available, tap to view"

    if (viewCurrentBundlePatches) {
        BundlePatchesDialog(
            onBackIcon = onBackIcon,
            onDismissRequest = {
                viewCurrentBundlePatches = false
            },
            topBarTitle = stringResource(R.string.bundle_patches),
            source = source,
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Scaffold(
            topBar = {
                BundleTopBar(
                    title = topBarTitle,
                    onBackClick = onDismissRequest,
                    onBackIcon = onBackIcon,
                    actions = {
                        IconButton(onClick = onDeleteRequest) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                "Delete"
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Outlined.Refresh,
                                "Refresh"
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        top = 16.dp,
                        end = 24.dp,
                    )
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        value = source.name,
                        onValueChange = {},
                        label = {
                            Text(stringResource(R.string.bundle_input_name))
                        }
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        value = remoteName,
                        onValueChange = {},
                        label = {
                            Text(stringResource(R.string.bundle_input_source_url))
                        }
                    )
                }
                Column(
                    Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 4.dp,
                    )
                ) {
                    BundleInfoListItem(
                        headlineText = stringResource(R.string.automatically_update),
                        supportingText = stringResource(R.string.automatically_update_description),
                        trailingContent = {
                            Switch(
                                checked = checked,
                                onCheckedChange = {
                                    checked = it
                                }
                            )
                        }
                    )

                    BundleInfoListItem(
                        headlineText = stringResource(R.string.bundle_type),
                        supportingText = stringResource(R.string.bundle_type_description),
                        trailingContent = {
                            FilledTonalButton(
                                onClick = { /* TODO */ },
                                content = {
                                    Text("Remote")
                                }
                            )
                        }
                    )

                    Text(
                        text = "Information",
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    BundleInfoListItem(
                        headlineText = stringResource(R.string.patches),
                        supportingText = patchInfoText,
                        trailingContent = {
                            if (patchCount > 0) {
                                IconButton(onClick = { viewCurrentBundlePatches = true }) {
                                    Icon(
                                        Icons.Outlined.ArrowRight,
                                        stringResource(R.string.patches)
                                    )
                                }
                            }
                        }
                    )

                    BundleInfoListItem(
                        headlineText = stringResource(R.string.patches_version),
                        supportingText = "1.0.0",
                    )

                    BundleInfoListItem(
                        headlineText = stringResource(R.string.integrations_version),
                        supportingText = "1.0.0",
                    )
                }
            }
        }
    }
}