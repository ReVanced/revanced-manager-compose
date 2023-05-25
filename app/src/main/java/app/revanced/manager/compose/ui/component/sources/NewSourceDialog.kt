package app.revanced.manager.compose.ui.component.sources

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.revanced.manager.compose.R
import app.revanced.manager.compose.ui.screen.NewSourceResult
import app.revanced.manager.compose.util.parseUrlOrNull

@Composable
fun NewSourceDialog(onDismissRequest: () -> Unit, onSubmit: (NewSourceResult) -> Unit) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Filled.Cancel, stringResource(R.string.cancel))
                }
                var isLocal by rememberSaveable { mutableStateOf(false) }
                var patchBundle by rememberSaveable { mutableStateOf<Uri?>(null) }
                var integrations by rememberSaveable { mutableStateOf<Uri?>(null) }
                var remoteUrl by rememberSaveable { mutableStateOf("") }

                var name by rememberSaveable { mutableStateOf("") }

                val inputsAreValid by remember {
                    derivedStateOf {
                        val nameSize = name.length

                        nameSize in 4..19 && if (isLocal) patchBundle != null else {
                            remoteUrl.isNotEmpty() && remoteUrl.parseUrlOrNull() != null
                        }
                    }
                }

                LaunchedEffect(isLocal) {
                    integrations = null
                    patchBundle = null
                    remoteUrl = ""
                }

                Text(text = if (isLocal) "Local" else "Remote")
                Switch(checked = isLocal, onCheckedChange = { isLocal = it })

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text("Name")
                    }
                )

                if (isLocal) {
                    LocalBundleSelectors(
                        onPatchesSelection = { patchBundle = it },
                        onIntegrationsSelection = { integrations = it },
                    )
                } else {
                    TextField(
                        value = remoteUrl,
                        onValueChange = { remoteUrl = it },
                        label = {
                            Text("API Url")
                        }
                    )
                }

                Button(
                    onClick = {
                        val result =
                            if (isLocal) NewSourceResult.Local(
                                name,
                                patchBundle!!,
                                integrations
                            ) else NewSourceResult.Remote(
                                name,
                                remoteUrl.parseUrlOrNull()!!
                            )
                        onSubmit(result)
                    },
                    enabled = inputsAreValid
                ) {
                    Text("Save")
                }
            }
        }
    }
}