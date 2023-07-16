package app.revanced.manager.ui.component.sources

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import app.revanced.manager.util.APK_MIMETYPE
import app.revanced.manager.util.JAR_MIMETYPE
import app.revanced.manager.util.parseUrlOrNull
import io.ktor.http.Url

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBundleDialog(
    onDismissRequest: () -> Unit,
    onRemoteSubmit: (String, Url) -> Unit,
    onLocalSubmit: (String, Uri, Uri?) -> Unit,
    patchCount: Int = 0,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var remoteUrl by rememberSaveable { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }
    var isLocal by rememberSaveable { mutableStateOf(false) }
    var patchBundle by rememberSaveable { mutableStateOf<Uri?>(null) }
    var integrations by rememberSaveable { mutableStateOf<Uri?>(null) }

    val patchBundleText = if (patchBundle == null) "" else patchBundle.toString()
    val integrationText = if (integrations == null) "" else integrations.toString()

    val inputsAreValid by remember {
        derivedStateOf {
            val nameSize = name.length
            nameSize in 4..19 && if (isLocal) patchBundle != null else {
                remoteUrl.isNotEmpty() && remoteUrl.parseUrlOrNull() != null
            }
        }
    }

    val patchActivityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { patchBundle = it }
        }

    val integrationsActivityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { integrations = it }
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
                    title = stringResource(R.string.import_bundle),
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
                                        if (isLocal) {
                                            onLocalSubmit(name, patchBundle!!, integrations)
                                        } else {
                                            onRemoteSubmit(name, remoteUrl.parseUrlOrNull()!!)
                                        }
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
                    BundleTextContent(
                        name = name,
                        onNameChange = { name = it },
                        isLocal = isLocal,
                        remoteUrl = remoteUrl,
                        onRemoteUrlChange = { remoteUrl = it },
                        patchBundleText = patchBundleText,
                        onPatchLauncherClick = {
                            patchActivityLauncher.launch(JAR_MIMETYPE)
                        },
                        integrationText = integrationText,
                        onIntegrationLauncherClick = {
                            integrationsActivityLauncher.launch(APK_MIMETYPE)
                        },
                    )
                }

                Column(
                    Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 4.dp,
                    )
                ) {
                    BundleInfoContent(
                        switchChecked = checked,
                        onCheckedChange = { checked = it },
                        patchInfoText = "No Patches available to view",
                        patchCount = patchCount,
                        onArrowClick = {},
                        tonalButtonContent = {
                            if (isLocal) {
                                Text("Local")
                            } else {
                                Text("Remote")
                            }
                        },
                        tonalButtonOnClick = { isLocal = !isLocal },
                        isLocal = isLocal,
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
@Composable
fun BundleTextContent(
    name: String,
    isImportPage: Boolean = true,
    onNameChange: (String) -> Unit = {},
    isLocal: Boolean,
    remoteUrl: String,
    onRemoteUrlChange: (String) -> Unit = {},
    patchBundleText: String,
    onPatchLauncherClick: () -> Unit = {},
    integrationText: String = "",
    onIntegrationLauncherClick: () -> Unit = {},
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        value = name,
        onValueChange = onNameChange,
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
            onValueChange = onRemoteUrlChange,
            label = {
                Text(stringResource(R.string.bundle_input_source_url))
            }
        )
    } else {
        if(isImportPage) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                value = patchBundleText,
                onValueChange = {},
                label = {
                    Text("Patches Source File")
                },
                trailingIcon = {
                    IconButton(
                        onClick = onPatchLauncherClick
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
                value = integrationText,
                onValueChange = {},
                label = {
                    Text("Integrations Source File")
                },
                trailingIcon = {
                    IconButton(onClick = onIntegrationLauncherClick) {
                        Icon(
                            imageVector = Icons.Default.Topic,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    }
}