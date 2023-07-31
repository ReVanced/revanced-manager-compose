package app.revanced.manager.data.room.bundles

import androidx.room.*
import io.ktor.http.*

sealed class Source {
    object Local : Source() {
        const val SENTINEL = "local"

        override fun toString() = SENTINEL
    }

    data class Remote(val url: Url) : Source() {
        override fun toString() = url.toString()
    }
}

data class VersionInfo(
    @ColumnInfo(name = "version") val patches: String,
    @ColumnInfo(name = "integrations_version") val integrations: String,
)

@Entity(tableName = "patch_bundles", indices = [Index(value = ["name"], unique = true)])
data class PatchBundleEntity(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @Embedded val versionInfo: VersionInfo,
    @ColumnInfo(name = "source") val source: Source,
    @ColumnInfo(name = "auto_update") val autoUpdate: Boolean
)

data class BundleProperties(
    @Embedded val versionInfo: VersionInfo,
    @ColumnInfo(name = "auto_update") val autoUpdate: Boolean
) {
    companion object {
        val BundleProperties.version get() = versionInfo.patches.takeUnless { it.isEmpty() }
    }
}