package app.revanced.manager.ui.component


import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.domain.sources.LocalSource
import app.revanced.manager.domain.sources.RemoteSource
import app.revanced.manager.domain.sources.Source
import app.revanced.manager.ui.component.bundle.BundleInformationDialog
import app.revanced.manager.ui.component.bundle.LocalBundleSelectors
import app.revanced.manager.ui.viewmodel.SourcesViewModel
import app.revanced.manager.util.uiSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream

@Composable
fun SourceItem(
    source: Source, onDelete: () -> Unit,
    coroutineScope: CoroutineScope,
) {
    var viewBundleDialogPage by remember { mutableStateOf(false) }

    val bundle by source.bundle.collectAsStateWithLifecycle()
    val patchCount = bundle.patches.size
    val padding = PaddingValues(16.dp, 0.dp)

    val androidContext = LocalContext.current

    val remoteName = if(source is RemoteSource) source.remoteUrl.toString() else ""

    if (viewBundleDialogPage) {
        BundleInformationDialog(
            onDismissRequest = { viewBundleDialogPage = false },
            onDeleteRequest = {
                viewBundleDialogPage = false
                onDelete()
            },
            onBackIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null
                )
            },
            topBarTitle = stringResource(R.string.bundle_information),
            source = source,
            patchCount = patchCount,
            remoteName = remoteName,
            onRefreshButton = {
                coroutineScope.launch {
                    uiSafe(
                        androidContext,
                        R.string.source_download_fail,
                        SourcesViewModel.failLogMsg
                    ) {
                        if (source is RemoteSource) source.update()
                    }
                }
            },
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .clickable {
                viewBundleDialogPage = true
            }
    ) {
        Text(
            text = source.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(padding)
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = pluralStringResource(R.plurals.patches_count, patchCount, patchCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun RemoteSourceItem(source: RemoteSource, coroutineScope: CoroutineScope) {
    val androidContext = LocalContext.current
    Text(text = "(api url here)")

    Button(onClick = {
        coroutineScope.launch {
            uiSafe(androidContext, R.string.source_download_fail, SourcesViewModel.failLogMsg) {
                source.update()
            }
        }
    }) {
        Text(text = "Check for updates")
    }
}

@Composable
private fun LocalSourceItem(source: LocalSource, coroutineScope: CoroutineScope) {
    val androidContext = LocalContext.current
    val resolver = remember { androidContext.contentResolver!! }

    fun loadAndReplace(
        uri: Uri,
        @StringRes toastMsg: Int,
        errorLogMsg: String,
        callback: suspend (InputStream) -> Unit
    ) = coroutineScope.launch {
        uiSafe(androidContext, toastMsg, errorLogMsg) {
            resolver.openInputStream(uri)!!.use {
                callback(it)
            }
        }
    }

    LocalBundleSelectors(
        onPatchesSelection = { uri ->
            loadAndReplace(uri, R.string.source_replace_fail, "Failed to replace patch bundle") {
                source.replace(it, null)
            }
        },
        onIntegrationsSelection = { uri ->
            loadAndReplace(
                uri,
                R.string.source_replace_integrations_fail,
                "Failed to replace integrations"
            ) {
                source.replace(null, it)
            }
        }
    )
}