package app.revanced.manager.ui.component.sources

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.revanced.manager.R
import app.revanced.manager.ui.component.BundleTopBar
import app.revanced.manager.util.parseUrlOrNull
import io.ktor.http.Url

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBundleDialog(
    onDismissRequest: () -> Unit,
    onRemoteSubmit: (String, Url) -> Unit,
    onLocalSubmit: (String, Uri, Uri?) -> Unit,
    topBarTitle: String,
    patchCount: Int = 0,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var remoteUrl by rememberSaveable { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }
    var isLocal by rememberSaveable { mutableStateOf(false) }
    var patchBundle by rememberSaveable { mutableStateOf<Uri?>(null) }
    var integrations by rememberSaveable { mutableStateOf<Uri?>(null) }

    val inputsAreValid by remember {
        derivedStateOf {
            val nameSize = name.length
            nameSize in 4..19 && if (isLocal) patchBundle != null else {
                remoteUrl.isNotEmpty() && remoteUrl.parseUrlOrNull() != null
            }
        }
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
                    onBackIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    },
                    actions = {
                        Text(
                            text = stringResource(R.string.import_),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {
                                    if (inputsAreValid) {
                                        onRemoteSubmit(name, remoteUrl.parseUrlOrNull()!!)
                                    }
                                }
                        )
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
                    if (isLocal) {
                        LocalBundleSelectors(
                            onPatchesSelection = { patchBundle = it },
                            onIntegrationsSelection = { integrations = it },
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text(stringResource(R.string.bundle_input_name))
                        }
                    )
                    if (!isLocal) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = remoteUrl,
                            onValueChange = { remoteUrl = it },
                            label = {
                                Text(stringResource(R.string.bundle_input_source_url))
                            }
                        )
                    } else {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = remoteUrl,
                            onValueChange = { remoteUrl = it },
                            label = {
                                Text("Patches Source File")
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {}
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Topic,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = remoteUrl,
                            onValueChange = { remoteUrl = it },
                            label = {
                                Text("Integrations Source File")
                            },
                            trailingIcon = {
                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.Topic,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }

                Column(
                    Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 4.dp,
                    )
                ) {
                    if (!isLocal) {
                        BundleInfoListItem(
                            headlineText = stringResource(R.string.automatically_update),
                            supportingText = stringResource(R.string.automatically_update_description),
                            trailingContent = {
                                Switch(
                                    checked = checked,
                                    onCheckedChange = { checked = it }
                                )
                            }
                        )
                    }


                    BundleInfoListItem(
                        headlineText = stringResource(R.string.bundle_type),
                        supportingText = stringResource(R.string.bundle_type_description)
                    ) {
                        FilledTonalButton(
                            onClick = { isLocal = !isLocal },
                            content = {
                                if (isLocal) {
                                    Text("Local")
                                } else {
                                    Text("Remote")
                                }
                            }
                        )
                    }

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
                        supportingText = "No Patches available to view",
                        trailingContent = {
                            if (patchCount > 0) {
                                IconButton(onClick = {}) {
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

@Composable
fun BundleInfoListItem(
    headlineText: String,
    supportingText: String = "",
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(
                text = headlineText,
                style = MaterialTheme.typography.titleLarge
            )
        },
        supportingContent = {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        },
        trailingContent = trailingContent,
    )
}