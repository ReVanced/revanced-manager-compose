package app.revanced.manager.ui.component.bundle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.revanced.manager.R

@Composable
inline fun BaseBundleDialog(
    modifier: Modifier = Modifier,
    name: String,
    noinline onNameChange: (String) -> Unit = {},
    remoteUrl: String?,
    noinline onRemoteUrlChange: (String) -> Unit = {},
    patchCount: Int,
    version: String?,
    autoUpdate: Boolean,
    noinline onAutoUpdateChange: (Boolean) -> Unit,
    noinline onPatchesClick: () -> Unit,
    noinline onBundleTypeClick: () -> Unit = {},
    extraFields: @Composable ColumnScope.() -> Unit = {}
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .then(modifier)
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
            onValueChange = onNameChange,
            label = {
                Text(stringResource(R.string.bundle_input_name))
            }
        )
        remoteUrl?.let {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                value = it,
                onValueChange = onRemoteUrlChange,
                label = {
                    Text(stringResource(R.string.bundle_input_source_url))
                }
            )
        }

        extraFields()
    }

    Column(
        Modifier.padding(
            start = 8.dp,
            top = 8.dp,
            end = 4.dp,
        )
    ) content@{
        if (remoteUrl == null) {
            BundleInfoListItem(
                headlineText = stringResource(R.string.automatically_update),
                supportingText = stringResource(R.string.automatically_update_description),
                trailingContent = {
                    Switch(
                        checked = autoUpdate,
                        onCheckedChange = onAutoUpdateChange
                    )
                }
            )
        }

        BundleInfoListItem(
            headlineText = stringResource(R.string.bundle_type),
            supportingText = stringResource(R.string.bundle_type_description)
        ) {
            FilledTonalButton(
                onClick = onBundleTypeClick,
                content = {
                    if (remoteUrl != null) {
                        Text(stringResource(R.string.remote))
                    } else {
                        Text(stringResource(R.string.local))
                    }
                }
            )
        }

        if (version == null && patchCount < 1) return@content

        Text(
            text = stringResource(R.string.information),
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        BundleInfoListItem(
            headlineText = stringResource(R.string.patches),
            supportingText = if (patchCount == 0) stringResource(R.string.no_patches)
            else stringResource(R.string.patches_available, patchCount),
            trailingContent = {
                if (patchCount > 0) {
                    IconButton(onClick = onPatchesClick) {
                        Icon(
                            Icons.Outlined.ArrowRight,
                            stringResource(R.string.patches)
                        )
                    }
                }
            }
        )

        if (version == null) return@content

        BundleInfoListItem(
            headlineText = stringResource(R.string.version),
            supportingText = version,
        )
    }
}