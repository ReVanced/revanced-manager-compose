package app.revanced.manager.compose.ui.component.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.revanced.manager.compose.destination.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSection(
    title: String,
    description: String,
    icon: ImageVector,
    onClick : () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.small.copy(CornerSize(0.dp)),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            headlineText = { Text(title, fontSize = 22.sp) },
            supportingText = { Text(description, fontSize = 14.sp) },
        )
    }
}

@Composable
fun SettingsHeader(
    title: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            color = color,
            fontSize = LocalTextStyle.current.fontSize.times(0.95f),
            fontWeight = FontWeight.SemiBold
        )
    }
}