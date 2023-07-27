package app.revanced.manager.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.domain.sources.RemoteSource
import app.revanced.manager.domain.sources.Source
import app.revanced.manager.ui.component.bundle.BundleInformationDialog
import app.revanced.manager.ui.viewmodel.SourcesViewModel
import app.revanced.manager.util.uiSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SourceItem(
    source: Source,
    onDelete: () -> Unit,
    coroutineScope: CoroutineScope,
) {
    var viewBundleDialogPage by rememberSaveable { mutableStateOf(false) }
    val state by source.bundle.collectAsStateWithLifecycle()

    val version by remember {
        source.version()
    }.collectAsStateWithLifecycle(null)

    val androidContext = LocalContext.current

    if (viewBundleDialogPage) {
        BundleInformationDialog(
            onDismissRequest = { viewBundleDialogPage = false },
            onDeleteRequest = {
                viewBundleDialogPage = false
                onDelete()
            },
            source = source,
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

    ListItem(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .clickable {
                viewBundleDialogPage = true
            },
        headlineContent = {
            Text(
                text = source.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            state.bundleOrNull()?.patches?.size?.let { patchCount ->
                Text(
                    text = pluralStringResource(R.plurals.patches_count, patchCount, patchCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        trailingContent = {
            Row {
                val icon = remember(state) {
                    when (state) {
                        is Source.State.Failed -> Icons.Outlined.ErrorOutline
                        is Source.State.Missing -> Icons.Outlined.Warning
                        is Source.State.Loaded -> null
                    }
                }

                icon?.let { vector ->
                    // TODO: set contentDescription
                    Icon(
                        imageVector = vector,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                version?.let { txt ->
                    Text(
                        text = txt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}