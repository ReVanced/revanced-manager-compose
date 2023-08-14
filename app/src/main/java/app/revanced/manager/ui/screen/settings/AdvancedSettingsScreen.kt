package app.revanced.manager.ui.screen.settings

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import app.revanced.manager.R
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.service.ShizukuApi
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.GroupHeader
import app.revanced.manager.ui.viewmodel.AdvancedSettingsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    onBackClick: () -> Unit, vm: AdvancedSettingsViewModel = getViewModel()
) {
    val prefs = vm.prefs
    val context = LocalContext.current
    var showInstallerPicker by rememberSaveable { mutableStateOf(false) }
    val memoryLimit = remember {
        val activityManager = context.getSystemService<ActivityManager>()!!
        context.getString(
            R.string.device_memory_limit_format,
            activityManager.memoryClass,
            activityManager.largeMemoryClass
        )
    }

    if (showInstallerPicker) {
        InstallerPicker(onDismiss = { showInstallerPicker = false },
            onConfirm = { vm.setInstaller(it) })
    }

    Scaffold(topBar = {
        AppTopBar(
            title = stringResource(R.string.advanced), onBackClick = onBackClick
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            val apiUrl by vm.apiUrl.getAsState()
            var showApiUrlDialog by rememberSaveable { mutableStateOf(false) }

            if (showApiUrlDialog) {
                APIUrlDialog(apiUrl) {
                    showApiUrlDialog = false
                    it?.let(vm::setApiUrl)
                }
            }
            ListItem(headlineContent = { Text(stringResource(R.string.api_url)) },
                supportingContent = { Text(apiUrl) },
                modifier = Modifier.clickable {
                    showApiUrlDialog = true
                })

            GroupHeader(stringResource(R.string.patch_bundles_section))
            ListItem(headlineContent = { Text(stringResource(R.string.patch_bundles_redownload)) },
                modifier = Modifier.clickable {
                    vm.redownloadBundles()
                })
            ListItem(headlineContent = { Text(stringResource(R.string.patch_bundles_reset)) },
                modifier = Modifier.clickable {
                    vm.resetBundles()
                })

            val installer by prefs.defaultInstaller.getAsState()
            GroupHeader(stringResource(R.string.installer))
            ListItem(modifier = Modifier.clickable { showInstallerPicker = true },
                headlineContent = { Text(stringResource(R.string.installer)) },
                supportingContent = { Text(stringResource(R.string.installer_description)) },
                trailingContent = {
                    FilledTonalButton(colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ), onClick = {
                        showInstallerPicker = true
                    }) {
                        Text(stringResource(installer.displayName))
                    }
                })

            GroupHeader(stringResource(R.string.device))
            ListItem(headlineContent = { Text(stringResource(R.string.device_model)) },
                supportingContent = { Text(Build.MODEL) })
            ListItem(headlineContent = { Text(stringResource(R.string.device_android_version)) },
                supportingContent = { Text(Build.VERSION.RELEASE) })
            ListItem(headlineContent = { Text(stringResource(R.string.device_architectures)) },
                supportingContent = { Text(Build.SUPPORTED_ABIS.joinToString(", ")) })
            ListItem(headlineContent = { Text(stringResource(R.string.device_memory_limit)) },
                supportingContent = { Text(memoryLimit) })
        }
    }
}

@Composable
private fun APIUrlDialog(currentUrl: String, onSubmit: (String?) -> Unit) {
    var url by rememberSaveable(currentUrl) { mutableStateOf(currentUrl) }

    AlertDialog(onDismissRequest = { onSubmit(null) }, confirmButton = {
        TextButton(onClick = {
            onSubmit(url)
        }) {
            Text(stringResource(R.string.api_url_dialog_save))
        }
    }, dismissButton = {
        TextButton(onClick = { onSubmit(null) }) {
            Text(stringResource(R.string.cancel))
        }
    }, icon = {
        Icon(Icons.Outlined.Http, null)
    }, title = {
        Text(
            text = stringResource(R.string.api_url_dialog_title),
            style = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }, text = {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.api_url_dialog_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.api_url_dialog_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            OutlinedTextField(value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.api_url)) })
        }
    })
}

@Composable
private fun InstallerPicker(
    onDismiss: () -> Unit,
    onConfirm: (PreferencesManager.InstallerManager) -> Unit,
    prefs: PreferencesManager = koinInject()
) {
    var selectedInstaller by rememberSaveable { mutableStateOf(prefs.defaultInstaller.getBlocking()) }
    val context: Context = LocalContext.current

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.installer)) },
        text = {
            Column {
                PreferencesManager.InstallerManager.values().forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedInstaller = it },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedInstaller == it,
                            onClick = { selectedInstaller = it })
                        Text(stringResource(it.displayName))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedInstaller == PreferencesManager.InstallerManager.SHIZUKU && !ShizukuApi.isShizukuPermissionGranted()) {
                    Toast.makeText(
                        context, R.string.shizuku_unavailable, Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                onConfirm(selectedInstaller)
                onDismiss()
            }) {
                Text(stringResource(R.string.apply))
            }
        })
}