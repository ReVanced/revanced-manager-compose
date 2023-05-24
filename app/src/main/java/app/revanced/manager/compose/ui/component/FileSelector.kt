package app.revanced.manager.compose.ui.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun FileSelector(mime: String, onSelect: (Uri) -> Unit, content: @Composable (launch: () -> Unit) -> Unit) {
    val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onSelect)
    }

    content {
        activityLauncher.launch(mime)
    }
}