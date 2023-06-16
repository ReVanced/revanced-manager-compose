package app.revanced.manager.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun ArrowButton(expanded: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (expanded) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "collapse"
            )
        } else {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "expand"
            )
        }
    }
}