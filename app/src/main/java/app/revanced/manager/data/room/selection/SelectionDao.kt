package app.revanced.manager.data.room.selection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SelectionDao {
    @Transaction
    @Query(
        "SELECT source, selected_patches.patch_name FROM patch_selections" +
                " LEFT JOIN selected_patches ON source = selected_patches.selection" +
                " WHERE package_name = :packageName"
    )
    suspend fun getSelectedPatches(packageName: String): Map<Int, List<String>>

    @Query("SELECT uid FROM patch_selections WHERE source = :sourceId AND package_name = :packageName")
    suspend fun getSelectionId(sourceId: Int, packageName: String): Int?

    @Query("DELETE FROM patch_selections")
    suspend fun reset()

    @Insert
    suspend fun createSelection(selection: PatchSelection)

    @Insert
    suspend fun selectPatches(patches: List<SelectedPatch>)

    @Query("DELETE FROM selected_patches WHERE selection = :selectionId")
    suspend fun clearSelection(selectionId: Int)

    @Transaction
    suspend fun updateSelection(uid: Int, patches: List<String>) {
        clearSelection(uid)
        selectPatches(patches.map { SelectedPatch(uid, it) })
    }
}