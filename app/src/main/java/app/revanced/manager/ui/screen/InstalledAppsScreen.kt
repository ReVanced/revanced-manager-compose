package app.revanced.manager.ui.screen

import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.data.room.apps.installed.InstalledApp
import app.revanced.manager.ui.component.AppIcon
import app.revanced.manager.ui.component.AppLabel
import app.revanced.manager.ui.component.LoadingIndicator
import app.revanced.manager.ui.viewmodel.InstalledAppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel

@Composable
fun InstalledAppsScreen(
    onAppClick: (InstalledApp) -> Unit,
    viewModel: InstalledAppsViewModel = getViewModel()
) {
    val installedApps by viewModel.apps.collectAsStateWithLifecycle(initialValue = null)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        installedApps?.let { installedApps ->

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    installedApps,
                    key = { it.currentPackageName }
                ) { installedApp ->
                    InstalledAppItem(
                        installedApp = installedApp,
                        onClick = { onAppClick(installedApp) },
                        deleteApp = { viewModel.delete(installedApp) }
                    )
                }
            }

            installedApps.ifEmpty {
                Text(
                    text = stringResource(R.string.no_patched_apps_found),
                    style = MaterialTheme.typography.titleLarge
                )
            }

        } ?: LoadingIndicator()
    }
}

@Suppress("DEPRECATION")
@Composable
fun InstalledAppItem(
    installedApp: InstalledApp,
    onClick: () -> Unit,
    deleteApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val packageManager = LocalContext.current.packageManager

    var appInfo: PackageInfo? by rememberSaveable { mutableStateOf(null) }
    
    LaunchedEffect(Unit) {
        try {
            appInfo = withContext(Dispatchers.IO) {
                packageManager.getPackageInfo(installedApp.currentPackageName, 0)
            }
        } catch (e: NameNotFoundException) {
            deleteApp()
        }
    }

    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
        leadingContent = { AppIcon(packageInfo = appInfo, contentDescription = null, Modifier.size(36.dp)) },
        headlineContent = { AppLabel(packageInfo = appInfo, defaultText = null) },
        supportingContent = { Text(installedApp.currentPackageName) }
    )
}