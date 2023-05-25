package app.revanced.manager.compose.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.compose.R
import app.revanced.manager.compose.ui.component.sources.NewSourceDialog
import app.revanced.manager.compose.ui.component.sources.SourceItem
import app.revanced.manager.compose.ui.viewmodel.SourcesScreenViewModel
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
            SourceItem(
                name = name,
                source = source,
                onDelete = {
                    vm.deleteSource(source)
                }
            )
        }

        Button(onClick = vm::redownloadAllSources) {
            Text(stringResource(R.string.reload_sources))
        }

        Button(onClick = { vm.showNewSourceDialog = true }) {
            Text("Create new source")
        }

        Button(onClick = vm::deleteAllSources) {
            Text("Reset everything.")
        }
    }
}