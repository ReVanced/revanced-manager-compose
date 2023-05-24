package app.revanced.manager.compose.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.compose.R
import app.revanced.manager.compose.domain.manager.sources.RemoteSource
import app.revanced.manager.compose.ui.viewmodel.SourcesScreenViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SourcesScreen(vm: SourcesScreenViewModel = getViewModel()) {
    val sources by vm.sources.collectAsStateWithLifecycle()

    if (vm.showNewSourceDialog) NewSourceDialog { vm.showNewSourceDialog = false }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        sources.forEach { (name, source) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(64.dp)
                    .clickable {
                        if (source is RemoteSource) {
                            vm.doUpdate {
                                source.update()
                            }
                        }
                    }
            ) {
                val bundle by source.bundle.collectAsStateWithLifecycle()
                val patchCount = bundle.patches.size
                val padding = PaddingValues(16.dp, 0.dp)

                Text(
                    text = name,
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

        Button(onClick = vm::redownloadAllSources) {
            Text(stringResource(R.string.reload_sources))
        }

        Button(onClick = { vm.showNewSourceDialog = true }) {
            Text("cossal will explod")
        }
    }
}

@Composable
fun NewSourceDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Filled.Cancel, stringResource(R.string.cancel))
                }
                var isLocal by rememberSaveable { mutableStateOf(false) }

                Text(text = if (isLocal) "Local" else "Remote")
                Switch(checked = isLocal, onCheckedChange = { isLocal = it })

                if (isLocal) {
                    Text("yes")
                }
            }
        }
    }
}