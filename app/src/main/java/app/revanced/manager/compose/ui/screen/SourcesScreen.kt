package app.revanced.manager.compose.ui.screen

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import app.revanced.manager.compose.ui.component.FileSelector
import app.revanced.manager.compose.ui.viewmodel.SourcesScreenViewModel
import app.revanced.manager.compose.util.APK_MIMETYPE
import app.revanced.manager.compose.util.JAR_MIMETYPE
import app.revanced.manager.compose.util.parseUrlOrNull
import io.ktor.http.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

// TODO: use two separate callbacks instead of doing this.
sealed class NewSourceResult(val name: String) {
    class Local(name: String, val patches: Uri, val integrations: Uri?) : NewSourceResult(name)
    class Remote(name: String, val apiUrl: Url) : NewSourceResult(name)
}

@Composable
fun SourcesScreen(vm: SourcesScreenViewModel = getViewModel()) {
    val sources by vm.sources.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    if (vm.showNewSourceDialog) NewSourceDialog(
        onDismissRequest = { vm.showNewSourceDialog = false },
        onSubmit = {
            vm.showNewSourceDialog = false
            scope.launch {
                when (it) {
                    is NewSourceResult.Local -> vm.addLocal(it.name, it.patches, it.integrations)
                    is NewSourceResult.Remote -> vm.addRemote(it.name, it.apiUrl)
                }
            }
        }
    )

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

        Button(onClick = vm::deleteAllSources) {
            Text("Reset everything.")
        }
    }
}

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
                    patchBundle = null
                    integrations = null
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
                    Row {
                        FileSelector(
                            mime = JAR_MIMETYPE,
                            onSelect = { patchBundle = it }
                        ) { launch ->
                            Button(onClick = launch) {
                                Text("Patch bundle")
                            }
                        }
                        FileSelector(
                            mime = APK_MIMETYPE,
                            onSelect = { integrations = it }
                        ) { launch ->
                            Button(onClick = launch) {
                                Text("Integrations")
                            }
                        }
                    }
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