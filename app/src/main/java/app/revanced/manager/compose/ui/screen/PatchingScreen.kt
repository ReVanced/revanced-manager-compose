package app.revanced.manager.compose.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.revanced.manager.compose.ui.viewmodel.PatchingScreenViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PatchingScreen(input: Uri, selectedPatches: List<String>, vm: PatchingScreenViewModel = getViewModel { parametersOf(input, selectedPatches) }) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "amogus",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = vm.status.toString(),
            style = MaterialTheme.typography.titleLarge
        )
    }
}