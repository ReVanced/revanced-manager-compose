package app.revanced.manager.compose.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.compose.R
import app.revanced.manager.compose.domain.manager.sources.NetworkSource
import app.revanced.manager.compose.ui.viewmodel.SourcesScreenViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun SourcesScreen(vm: SourcesScreenViewModel = getViewModel()) {
    val coroutineScope = rememberCoroutineScope()
    val sources by vm.sources.collectAsStateWithLifecycle()

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
                        if (source is NetworkSource) {
                            coroutineScope.launch {
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

        Button(onClick = vm::update) {
            Text(stringResource(R.string.reload_sources))
        }
    }
}