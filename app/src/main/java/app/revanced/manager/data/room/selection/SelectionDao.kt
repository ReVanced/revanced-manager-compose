package app.revanced.manager.data.room.selection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class SelectionDao {
    @Transaction
    @MapInfo(keyColumn = "source", valueColumn = "patch_name")
    @Query(
        "SELECT source, patch_name FROM patch_selections" +
                " LEFT JOIN selected_patches ON uid = selected_patches.selection" +
                " WHERE package_name = :packageName"
    )
    abstract suspend fun getSelectedPatches(packageName: String): Map<Int, List<String>>

    @Query("SELECT uid FROM patch_selections WHERE source = :sourceId AND package_name = :packageName")
    abstract suspend fun getSelectionId(sourceId: Int, packageName: String): Int?

    @Query("DELETE FROM patch_selections")
    abstract suspend fun reset()

    @Insert
    abstract suspend fun createSelection(selection: PatchSelection)

    @Insert
    protected abstract suspend fun selectPatches(patches: List<SelectedPatch>)

    @Query("DELETE FROM selected_patches WHERE selection = :selectionId")
    protected abstract suspend fun clearSelection(selectionId: Int)

    @Transaction
    open suspend fun updateSelections(selections: Map<Int, Set<String>>) =
        selections.map { (selectionUid, patches) ->
            clearSelection(selectionUid)
            selectPatches(patches.map { SelectedPatch(selectionUid, it) })
        }
}