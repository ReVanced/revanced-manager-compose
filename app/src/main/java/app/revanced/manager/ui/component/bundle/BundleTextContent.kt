package app.revanced.manager.ui.component.bundle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.revanced.manager.R

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