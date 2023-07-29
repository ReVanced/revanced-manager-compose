package app.revanced.manager.data.room.sources

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM $sourcesTableName")
    suspend fun all(): List<SourceEntity>

    @Query("SELECT version, integrations_version, auto_update FROM $sourcesTableName WHERE uid = :uid")
    fun getPropsById(uid: Int): Flow<SourceProperties>

    @Query("UPDATE $sourcesTableName SET version=:patches, integrations_version=:integrations WHERE uid=:uid")
    suspend fun updateVersion(uid: Int, patches: String, integrations: String)

    @Query("UPDATE $sourcesTableName SET auto_update=:value WHERE uid=:uid")
    suspend fun setAutoUpdate(uid: Int, value: Boolean)

    @Query("DELETE FROM $sourcesTableName WHERE uid!=0")
    suspend fun purgeCustomSources()
    @Query("UPDATE $sourcesTableName SET version='', integrations_version='' WHERE uid=0")
    suspend fun resetMainSource()

    @Transaction
    suspend fun reset() {
        purgeCustomSources()
        resetMainSource()
    }

    @Query("DELETE FROM $sourcesTableName WHERE uid=:uid")
    suspend fun remove(uid: Int)

    @Insert
    suspend fun add(source: SourceEntity)
}