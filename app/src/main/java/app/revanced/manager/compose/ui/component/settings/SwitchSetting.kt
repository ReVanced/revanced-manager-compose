package app.revanced.manager.compose.ui.component.settings

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @author Hyperion Authors, zt64
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchSetting(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checked: Boolean,
    icon: ImageVector? = null,
    text: String,
    description: String? = null,
    onCheckedChange: (value: Boolean) -> Unit
) {
    ListItem(
        modifier = modifier.clickable(enabled) {
            onCheckedChange(!checked)
        },
        headlineText = { Text(text) },
        supportingText = description?.let { { Text(it) } },
        leadingContent = icon?.let { imageVector ->
            {
                Icon(
                    imageVector = imageVector,
                    contentDescription = text
                )
            }
        },
        trailingContent = {
            Switch(
                enabled = enabled,
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}