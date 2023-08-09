package app.revanced.manager.ui.component.patches

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.GroupHeader
import app.revanced.manager.util.saver.PathSaver
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathSelectorDialog(root: Path, onSelect: (Path?) -> Unit) {
    var currentDirectory by rememberSaveable(root, stateSaver = PathSaver) { mutableStateOf(root) }
    val notAtRootDir = remember(currentDirectory) {
        currentDirectory != root
    }
    val (directories, files) = remember(currentDirectory) {
        currentDirectory.listDirectoryEntries().filter(Path::isReadable).partition(Path::isDirectory)
    }

    Dialog(
        onDismissRequest = { onSelect(null) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.select_file),
                    onBackClick = { onSelect(null) },
                    backIcon = {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
                    }
                )
            },
        ) { paddingValues ->
            BackHandler(enabled = notAtRootDir) {
                currentDirectory = currentDirectory.parent
            }

            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                item(key = "current") {
                    PathItem(
                        onClick = { onSelect(currentDirectory) },
                        icon = Icons.Outlined.Folder,
                        name = currentDirectory.toString(),
                        description = "Click to select this directory"
                    )
                }

                if (notAtRootDir) {
                    item(key = "parent") {
                        PathItem(
                            onClick = { currentDirectory = currentDirectory.parent },
                            icon = Icons.Outlined.ArrowBack,
                            name = "Previous directory",
                        )
                    }
                }

                if (directories.isNotEmpty()) {
                    item(key = "dirs_header") {
                        GroupHeader(title = "Directories")
                    }
                }
                items(directories, key = { it.absolutePathString() }) {
                    PathItem(
                        onClick = { currentDirectory = it },
                        icon = Icons.Outlined.Folder,
                        name = it.name
                    )
                }

                if (files.isNotEmpty()) {
                    item(key = "files_header") {
                        GroupHeader(title = "Files")
                    }
                }
                items(files, key = { it.absolutePathString() }) {
                    PathItem(
                        onClick = { onSelect(it) },
                        icon = Icons.Outlined.InsertDriveFile,
                        name = it.name,
                    )
                }
            }
        }
    }
}

@Composable
private fun PathItem(
    onClick: () -> Unit,
    icon: ImageVector,
    name: String,
    description: String? = null
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(name) },
        leadingContent = { Icon(icon, contentDescription = null) },
        supportingContent = description?.let { { Text(it) } }
    )
}