package app.revanced.manager.data.room.apps.installed

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import app.revanced.manager.data.room.bundles.PatchBundleEntity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "applied_patch",
    primaryKeys = ["package_name", "bundle_uid", "patch_name"],
    foreignKeys = [
        ForeignKey(
            InstalledApp::class,
            parentColumns = ["current_package_name"],
            childColumns = ["package_name"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            PatchBundleEntity::class,
            parentColumns = ["uid"],
            childColumns = ["bundle_uid"]
        )
    ]
)
data class AppliedPatch(
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "bundle_uid") val bundleUid: Int,
    @ColumnInfo(name = "patch_name") val patchName: String
) : Parcelable