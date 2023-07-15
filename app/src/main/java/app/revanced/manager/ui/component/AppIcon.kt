package app.revanced.manager.ui.component

import android.content.pm.PackageInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AppIcon(
    packageInfo: PackageInfo?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (packageInfo == null) {
        val image = rememberVectorPainter(Icons.Default.Android)
        val colorFilter = ColorFilter.tint(LocalContentColor.current)

        Image(
            image,
            contentDescription,
            Modifier.size(36.dp).then(modifier),
            colorFilter = colorFilter
        )
    } else {
        AsyncImage(
            packageInfo,
            contentDescription,
            Modifier.size(36.dp).then(modifier)
        )
    }
}