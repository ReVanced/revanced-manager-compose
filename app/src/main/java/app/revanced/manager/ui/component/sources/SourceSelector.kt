package app.revanced.manager.ui.component.sources

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import app.revanced.manager.domain.sources.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceSelector(sources: List<Source>, onFinish: (Source?) -> Unit) {
    LaunchedEffect(sources) {
        if (sources.size == 1) {
            onFinish(sources[0])
        }
    }

    if (sources.size < 2) {
        return
    }

    ModalBottomSheet(
        onDismissRequest = { onFinish(null) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Select bundle",
                style = MaterialTheme.typography.titleMedium
            )
            sources.forEach {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onFinish(it) }
                )
            }
        }
    }
}