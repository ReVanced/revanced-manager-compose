package app.revanced.manager.compose.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    text: String
) {
    TopAppBar(
        title = {
            Text(text)
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null)
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithBackButton(
    text: String
) {
    TopAppBar(
        title = {
            Text(text)
        },
        navigationIcon = {
            IconButton(onClick = {

            }) {
                Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
            }
        }
    )
}
