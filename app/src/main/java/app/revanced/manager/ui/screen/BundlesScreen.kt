package app.revanced.manager.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.revanced.manager.R
import app.revanced.manager.ui.component.bundle.BundleItem
import app.revanced.manager.ui.viewmodel.BundlesViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun BundlesScreen(
    vm: BundlesViewModel = getViewModel(),
) {
    val sources by vm.sources.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        sources.forEach {
            BundleItem(
                source = it,
                onDelete = {
                    vm.delete(it)
                },
                coroutineScope = vm.viewModelScope
            )
        }

        Button(onClick = vm::redownloadAllSources) {
            Text(stringResource(R.string.reload_sources))
        }

        /*
        Button(onClick = { showNewSourceDialog = true }) {
            Text("Create new source")
        }
         */

        Button(onClick = vm::deleteAllSources) {
            Text("Reset everything.")
        }
    }
}