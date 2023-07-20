package app.revanced.manager.ui.component.bundle

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.revanced.manager.ui.component.ContentSelector
import app.revanced.manager.util.APK_MIMETYPE
import app.revanced.manager.util.JAR_MIMETYPE

@Composable
fun LocalBundleSelectors(onPatchesSelection: (Uri) -> Unit, onIntegrationsSelection: (Uri) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ContentSelector(
            mime = JAR_MIMETYPE,
            onSelect = onPatchesSelection
        ) {
            Text("Patches")
        }

        ContentSelector(
            mime = APK_MIMETYPE,
            onSelect = onIntegrationsSelection
        ) {
            Text("Integrations")
        }
    }
}