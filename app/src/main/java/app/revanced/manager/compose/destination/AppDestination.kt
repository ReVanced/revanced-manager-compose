package app.revanced.manager.compose.destination

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import app.revanced.manager.compose.R
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

sealed interface Destination: Parcelable {

    @Parcelize
    object Dashboard: DashboardDestination(
        Icons.Default.Home, Icons.Outlined.Home, R.string.dashboard
    )

    @Parcelize
    object Sources: DashboardDestination(
        Icons.Default.Home, Icons.Outlined.Home, R.string.tab_sources
    )

}

@Parcelize
sealed class DashboardDestination(
    val icon: @RawValue ImageVector,
    val outlinedIcon: @RawValue ImageVector,
    @StringRes val label: Int
) : Destination