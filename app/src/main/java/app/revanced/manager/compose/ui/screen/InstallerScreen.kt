package app.revanced.manager.compose.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.revanced.manager.compose.ui.component.AppScaffold
import app.revanced.manager.compose.ui.component.AppTopBar
import app.revanced.manager.compose.ui.viewmodel.InstallerScreenViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallerScreen(
    input: Uri,
    selectedPatches: List<String>,
    vm: InstallerScreenViewModel = getViewModel { parametersOf(input, selectedPatches) }
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Installer",
                onBackClick = { },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            vm.stepGroups.forEach {
                Column {
                    Text(
                        text = "${it.name}: ${it.status}",
                        style = MaterialTheme.typography.titleLarge
                    )

                    it.steps.forEach {
                        Text(
                            text = "${it.name}: ${it.status}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}