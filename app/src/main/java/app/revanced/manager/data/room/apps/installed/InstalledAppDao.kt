package app.revanced.manager.data.room.apps.installed

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledAppDao {
    @Query("SELECT * FROM installed_app")
    fun getAllApps(): Flow<List<InstalledApp>>

    @Query("SELECT * FROM installed_app WHERE current_package_name = :packageName")
    suspend fun get(packageName: String): InstalledApp?

    @Insert
    suspend fun insert(installedApp: InstalledApp)

    @Delete
    suspend fun delete(installedApp: InstalledApp)
}