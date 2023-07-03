package app.revanced.manager.ui.component.sources

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
    onLocalSubmit: (String, Uri, Uri?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var remoteUrl by rememberSaveable { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }
    var isLocal by rememberSaveable { mutableStateOf(false) }
    var patchBundle by rememberSaveable { mutableStateOf<Uri?>(null) }

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
                    title = stringResource(R.string.import_bundle),
                    onBackClick = onDismissRequest,
                    actions = {
                        Text(
                            text = stringResource(R.string.import_),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {
                                    if(inputsAreValid) {
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
                }

                Column(
                    Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 4.dp,
                        )
                ) {
                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.automatically_update))
                        },
                        supportingContent = {
                            Text(stringResource(R.string.automatically_update_description))
                        },
                        trailingContent = {
                            Switch(
                                checked = checked,
                                onCheckedChange = { checked = it }
                            )
                        },
                    )

                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.bundle_type))
                        },
                        supportingContent = {
                            Text(stringResource(R.string.bundle_type_description))
                        },
                        trailingContent = {
                            FilledTonalButton(
                                onClick = {},
                                content = {
                                    Text("Remote")
                                }
                            )
                        },
                    )

                    Text(
                        text = "Information",
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                            ),
                        style = MaterialTheme.typography.bodySmall
                    )

                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.patches))
                        },
                        supportingContent = {
                            Text("Other things")
                        },
                        trailingContent = {
                            IconButton(onClick = { }) {
                                Icon(Icons.Outlined.ArrowRight, stringResource(R.string.patches))
                            }
                        },
                    )

                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.patches_version))
                        },
                        supportingContent = {
                            Text("1.0.0")
                        }
                    )

                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.integrations_version))
                        },
                        supportingContent = {
                            Text("1.0.0")
                        }
                    )


                }
            }
        }
    }

}