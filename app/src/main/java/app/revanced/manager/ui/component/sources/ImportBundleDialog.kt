package app.revanced.manager.ui.component.sources

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.ktor.http.Url

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBundleDialog(
    onDismissRequest: () -> Unit,
    onRemoteSubmit: (String, Url) -> Unit,
    onLocalSubmit: (String, Uri, Uri?) -> Unit
) {
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
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                var name by rememberSaveable { mutableStateOf("") }
                var remoteUrl by rememberSaveable { mutableStateOf("") }
                var checked by remember { mutableStateOf(true) }

                Column(
                    modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 0.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 0.dp, 16.dp),
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text("Name")
                        }
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 0.dp, 16.dp),
                        value = remoteUrl,
                        onValueChange = { remoteUrl = it },
                        label = {
                            Text("Source URL")
                        }
                    )
                }

                Column(
                    Modifier.padding(8.dp, 8.dp, 4.dp, 0.dp)
                ) {
                    ListItem(
                        headlineContent = {
                            Text("Automatically update")
                        },
                        supportingContent = {
                            Text("Automatically update this bundle when ReVanced starts")
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
                            Text("Bundle type")
                        },
                        supportingContent = {
                            Text("Choose the type of bundle you want")
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

                }
            }
        }
    }

}