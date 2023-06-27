package app.revanced.manager.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.destination.SettingsDestination
import app.revanced.manager.ui.screen.settings.*
import app.revanced.manager.ui.viewmodel.SettingsViewModel
import dev.olshevski.navigation.reimagined.*
import org.koin.androidx.compose.getViewModel

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = getViewModel()
) {
    val navController =
        rememberNavController<SettingsDestination>(startDestination = SettingsDestination.Settings)

    val context = LocalContext.current
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    var showBatteryButton by remember { mutableStateOf(!pm.isIgnoringBatteryOptimizations(context.packageName)) }

    val settingsSections = listOf(
        Triple(
            R.string.general,
            R.string.general_description,
            Icons.Outlined.Settings
        ) to SettingsDestination.General,
        Triple(
            R.string.updates,
            R.string.updates_description,
            Icons.Outlined.Update
        ) to SettingsDestination.Updates,
        Triple(
            R.string.downloads,
            R.string.downloads_description,
            Icons.Outlined.Download
        ) to SettingsDestination.Downloads,
        Triple(
            R.string.import_export,
            R.string.import_export_description,
            Icons.Outlined.SwapVert
        ) to SettingsDestination.ImportExport,
        Triple(
            R.string.about,
            R.string.about_description,
            Icons.Outlined.Info
        ) to SettingsDestination.About,
    )
    NavBackHandler(navController)

    AnimatedNavHost(
        controller = navController
    ) { destination ->
        when (destination) {

            is SettingsDestination.General -> GeneralSettingsScreen(
                onBackClick = { navController.pop() },
                viewModel = viewModel
            )

            is SettingsDestination.Updates -> UpdatesSettingsScreen(
                onBackClick = { navController.pop() },
                navController = navController
            )

            is SettingsDestination.Downloads -> DownloadsSettingsScreen(
                onBackClick = { navController.pop() }
            )

            is SettingsDestination.ImportExport -> ImportExportSettingsScreen(
                onBackClick = { navController.pop() }
            )

            is SettingsDestination.About -> AboutSettingsScreen(
                onBackClick = { navController.pop() },
                onContributorsClick = { navController.navigate(SettingsDestination.Contributors) }
            )

            is SettingsDestination.UpdateProgress -> UpdateProgressScreen(
               { navController.pop() },
            )

            is SettingsDestination.Contributors -> ContributorScreen(
                onBackClick = { navController.pop() },
            )

            is SettingsDestination.Settings -> {
                Scaffold(
                    topBar = {
                        AppTopBar(
                            title = stringResource(R.string.settings),
                            onBackClick = onBackClick,
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedVisibility(visible = showBatteryButton) {
                            Card(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    })
                                    showBatteryButton =
                                        !pm.isIgnoringBatteryOptimizations(context.packageName)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryAlert,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.battery_optimization_notification),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        settingsSections.forEach { (titleDescIcon, destination) ->
                            ListItem(
                                modifier = Modifier.clickable { navController.navigate(destination) },
                                headlineContent = {
                                    Text(
                                        stringResource(titleDescIcon.first),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        stringResource(titleDescIcon.second),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                },
                                leadingContent = { Icon(titleDescIcon.third, null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
