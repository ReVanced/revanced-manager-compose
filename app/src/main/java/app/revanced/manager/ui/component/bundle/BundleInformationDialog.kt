package app.revanced.manager.ui.component.bundle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.domain.sources.LocalSource
import app.revanced.manager.domain.sources.RemoteSource
import app.revanced.manager.domain.sources.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BundleInformationDialog(
    onDismissRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    source: Source,
    onRefreshButton: () -> Unit,
) {
    var autoUpdate by remember { mutableStateOf(true) }
    var viewCurrentBundlePatches by remember { mutableStateOf(false) }
    val isLocal = source is LocalSource
    val version by source.version().collectAsStateWithLifecycle(initialValue = null)

    if (viewCurrentBundlePatches) {
        BundlePatchesDialog(
            onDismissRequest = {
                viewCurrentBundlePatches = false
            },
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
                    title = stringResource(R.string.bundle_information),
                    onBackClick = onDismissRequest,
                    onBackIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    },
                    actions = {
                        IconButton(onClick = onDeleteRequest) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                stringResource(R.string.delete)
                            )
                        }
                        if (!isLocal) {
                            IconButton(onClick = onRefreshButton) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    stringResource(R.string.refresh)
                                )
                            }
                        }
                    }
                )
            },
        ) { paddingValues ->
            BaseBundleDialog(
                modifier = Modifier.padding(paddingValues),
                name = source.name,
                remoteUrl = (source as? RemoteSource)?.apiUrl,
                patchCount = 21,
                version = version,
                autoUpdate = autoUpdate,
                onAutoUpdateChange = { autoUpdate = it },
                onPatchesClick = {
                    viewCurrentBundlePatches = true
                },
            )
        }
    }
}
