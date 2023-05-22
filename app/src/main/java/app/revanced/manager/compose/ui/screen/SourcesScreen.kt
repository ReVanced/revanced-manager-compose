package app.revanced.manager.compose.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.compose.domain.manager.sources.impl.NetworkSource
import app.revanced.manager.compose.ui.viewmodel.SourcesScreenViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SourcesScreen(vm: SourcesScreenViewModel = getViewModel()) {
    val sources by vm.sources.collectAsStateWithLifecycle(initialValue = emptyMap())

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        sources.forEach { (name, source) ->
            Row {
                Text(name)

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                if (source is NetworkSource) {
                    Button(onClick = { }) {
                        Text("Reload this source")
                    }
                }
            }
        }

        Button(onClick = vm::update) {
            Text("Update all sources")
        }
    }
}