package app.revanced.manager.domain.repository

import app.revanced.manager.data.room.AppDatabase
import app.revanced.manager.data.room.AppDatabase.Companion.generateUid
import app.revanced.manager.data.room.sources.SourceEntity
import app.revanced.manager.data.room.sources.SourceLocation
import app.revanced.manager.data.room.sources.VersionInfo
import app.revanced.manager.util.apiURL
import io.ktor.http.*
import kotlinx.coroutines.flow.distinctUntilChanged

class SourcePersistenceRepository(db: AppDatabase) {
    private val dao = db.sourceDao()

    private companion object {
        val defaultSource = SourceEntity(
            uid = generateUid(),
            name = "Official",
            versionInfo = VersionInfo("", ""),
            location = SourceLocation.Remote(Url(apiURL)),
            autoUpdate = false
        )
    }

    suspend fun loadConfiguration(): List<SourceEntity> {
        val all = dao.all()
        if (all.isEmpty()) {
            dao.add(defaultSource)
            return listOf(defaultSource)
        }

        return all
    }

    suspend fun clear() = dao.purge()

    suspend fun create(name: String, location: SourceLocation, autoUpdate: Boolean = false): Int {
        val uid = generateUid()
        dao.add(
            SourceEntity(
                uid = uid,
                name = name,
                versionInfo = VersionInfo("", ""),
                location = location,
                autoUpdate = autoUpdate
            )
        )

        return uid
    }

    suspend fun delete(uid: Int) = dao.remove(uid)

    suspend fun updateVersion(uid: Int, patches: String, integrations: String) =
        dao.updateVersion(uid, patches, integrations)
    suspend fun setAutoUpdate(uid: Int, value: Boolean) = dao.setAutoUpdate(uid, value)

    fun getProps(id: Int) = dao.getPropsById(id).distinctUntilChanged()
}