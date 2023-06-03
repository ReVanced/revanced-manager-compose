package app.revanced.manager.compose.ui.screen

import android.content.pm.PackageInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.compose.R
import app.revanced.manager.compose.ui.component.AppIcon
import app.revanced.manager.compose.ui.component.AppTopBar
import app.revanced.manager.compose.ui.component.LoadingIndicator
import app.revanced.manager.compose.ui.viewmodel.AppSelectorViewModel
import app.revanced.manager.compose.util.APK_MIMETYPE
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorScreen(
    onAppClick: (PackageInfo) -> Unit,
    onBackClick: () -> Unit,
    vm: AppSelectorViewModel = getViewModel()
) {
    val pickApkLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { apkUri -> vm.loadSelectedFile(apkUri).let(onAppClick) }
        }

    var filterText by rememberSaveable { mutableStateOf("") }
    var search by rememberSaveable { mutableStateOf(false) }

    val appList by vm.appList.collectAsStateWithLifecycle(initialValue = emptyList())
    val filteredAppList = rememberSaveable(appList, filterText) {
        appList.filter { app ->
            (vm.loadLabel(app.packageInfo)).contains(
                filterText,
                true
            ) or app.packageName.contains(filterText, true)
        }
    }

    // TODO: find something better for this
    if (search) {
        SearchBar(
            query = filterText,
            onQueryChange = { filterText = it },
            onSearch = { },
            active = true,
            onActiveChange = { search = it },
            modifier = Modifier.fillMaxSize(),
            placeholder = { Text(stringResource(R.string.search_apps)) },
            leadingIcon = {
                IconButton({ search = false }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        stringResource(R.string.back)
                    )
                }
            },
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (appList.isNotEmpty()) {
                        items(
                            items = filteredAppList,
                            key = { it.packageName }
                        ) { app ->

                            ListItem(
                                modifier = Modifier.clickable {
                                    app.packageInfo?.let { onAppClick(it) }
                                },
                                leadingContent = { AppIcon(app, null) },
                                headlineContent = { Text(vm.loadLabel(app.packageInfo)) },
                                supportingContent = { Text(app.packageName) },
                                trailingContent = { Text(app.patches.toString()) }
                            )

                        }
                    } else {
                        item { LoadingIndicator() }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.select_app),
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.HelpOutline, stringResource(R.string.help))
                    }
                    IconButton(onClick = { search = true }) {
                        Icon(Icons.Outlined.Search, stringResource(R.string.search))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            item {
                ListItem(
                    modifier = Modifier.clickable {
                        pickApkLauncher.launch(APK_MIMETYPE)
                    },
                    leadingContent = {
                        Box(Modifier.size(36.dp), Alignment.Center) {
                            Icon(
                                Icons.Default.Storage,
                                null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    headlineContent = { Text(stringResource(R.string.select_from_storage)) }
                )
                Divider()
            }

            if (appList.isNotEmpty()) {
                items(
                    items = appList,
                    key = { it.packageName }
                ) { app ->

                    ListItem(
                        modifier = Modifier.clickable {
                            app.packageInfo?.let { onAppClick(it) }
                        },
                        leadingContent = { AppIcon(app, null) },
                        headlineContent = { Text(vm.loadLabel(app.packageInfo)) },
                        supportingContent = { Text(app.packageName) },
                        trailingContent = { Text(app.patches.toString()) }
                    )

                }
            } else {
                item { LoadingIndicator() }
            }
        }
    }
}
