package app.revanced.manager.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.revanced.manager.R
import app.revanced.manager.service.ShizukuApi
import app.revanced.manager.ui.component.ShizukuCard

@Composable
fun InstalledAppsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (ShizukuApi.isShizukuInstalled()) ShizukuCard()

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.no_patched_apps_found),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}