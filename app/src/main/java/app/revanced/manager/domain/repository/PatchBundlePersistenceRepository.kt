package app.revanced.manager.domain.repository

import app.revanced.manager.data.room.AppDatabase
import app.revanced.manager.data.room.AppDatabase.Companion.generateUid
import app.revanced.manager.data.room.bundles.PatchBundleEntity
import app.revanced.manager.data.room.bundles.Source
import app.revanced.manager.data.room.bundles.VersionInfo
import io.ktor.http.*
import kotlinx.coroutines.flow.distinctUntilChanged

class PatchBundlePersistenceRepository(db: AppDatabase) {
    private val dao = db.patchBundleDao()

    private companion object {
        val defaultSource = PatchBundleEntity(
            uid = 0,
            name = "Main",
            versionInfo = VersionInfo("", ""),
            source = Source.Remote(Url("manager://api")),
            autoUpdate = false
        )
    }

    suspend fun loadConfiguration(): List<PatchBundleEntity> {
        val all = dao.all()
        if (all.isEmpty()) {
            dao.add(defaultSource)
            return listOf(defaultSource)
        }

        return all
    }

    suspend fun reset() = dao.reset()

    suspend fun create(name: String, source: Source, autoUpdate: Boolean = false): Int {
        val uid = generateUid()
        dao.add(
            PatchBundleEntity(
                uid = uid,
                name = name,
                versionInfo = VersionInfo("", ""),
                source = source,
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