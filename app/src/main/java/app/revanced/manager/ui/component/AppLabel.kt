package app.revanced.manager.ui.component

import android.content.pm.PackageInfo
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import app.revanced.manager.R
import com.google.accompanist.placeholder.placeholder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppLabel(
    packageInfo: PackageInfo?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    defaultText: String = stringResource(R.string.not_installed)
) {
    val context = LocalContext.current

    var label: String? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(packageInfo) {
        withContext(Dispatchers.IO) {
            packageInfo?.applicationInfo?.loadLabel(context.packageManager)
                ?.also {
                    withContext(Dispatchers.Main) {
                        label = it.toString()
                    }
                } ?: withContext(Dispatchers.Main) {
                    label = defaultText
                }
        }
    }

    Text(
        label ?: stringResource(R.string.loading),
        modifier = Modifier
            .placeholder(
                visible = label == null,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(100)
            )
            .then(modifier),
        style = style
    )
}