package app.revanced.manager.data.room.selection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.revanced.manager.data.room.sources.SourceEntity

@Entity(
    tableName = "patch_selections",
    foreignKeys = [ForeignKey(
        SourceEntity::class,
        parentColumns = ["uid"],
        childColumns = ["source"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["source", "package_name"], unique = true)]
)
data class PatchSelection(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "source") val source: Int,
    @ColumnInfo(name = "package_name") val packageName: String
)

data class FullSelection(
    @ColumnInfo(name = "uid") val selectionId: Int,
    @Relation(
        parentColumn = "uid",
        entityColumn = "selection"
    )
    val patches: List<SelectedPatch>
)