package app.revanced.manager.data.room.apps.installed

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

enum class InstallType(val displayName: String) {
    DEFAULT("Default"),
    ROOT("Root")
}

@Parcelize
@Entity(tableName = "installed_app")
data class InstalledApp(
    @PrimaryKey
    @ColumnInfo(name = "current_package_name") val currentPackageName: String,
    @ColumnInfo(name = "original_package_name") val originalPackageName: String,
    @ColumnInfo(name = "version") val version: String,
    @ColumnInfo(name = "install_type") val installType: InstallType
) : Parcelable