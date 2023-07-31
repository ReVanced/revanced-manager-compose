package app.revanced.manager.domain.repository

import android.app.Application
import android.content.Context
import android.util.Log
import app.revanced.manager.data.platform.NetworkInfo
import app.revanced.manager.data.room.bundles.PatchBundleEntity
import app.revanced.manager.data.room.bundles.Source as SourceInfo
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.bundles.LocalPatchBundle
import app.revanced.manager.domain.bundles.RemoteBundle
import app.revanced.manager.domain.bundles.BundleSource
import app.revanced.manager.util.flatMapLatestAndCombine
import app.revanced.manager.util.tag
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class PatchBundleRepository(
    app: Application,
    private val persistenceRepo: PatchBundlePersistenceRepository,
    private val networkInfo: NetworkInfo,
    private val prefs: PreferencesManager
) {
    private val bundlesDir = app.getDir("patch_bundles", Context.MODE_PRIVATE)

    private val _sources: MutableStateFlow<Map<Int, BundleSource>> = MutableStateFlow(emptyMap())
    val sources = _sources.map { it.values.toList() }

    val bundles = sources.flatMapLatestAndCombine(
        combiner = {
            it.mapNotNull { (uid, state) ->
                val bundle = state.patchBundleOrNull() ?: return@mapNotNull null
                uid to bundle
            }.toMap()
        }
    ) {
        it.state.map { state -> it.uid to state }
    }

    /**
     * Get the directory of the [BundleSource] with the specified [uid], creating it if needed.
     */
    private fun directoryOf(uid: Int) = bundlesDir.resolve(uid.toString()).also { it.mkdirs() }

    private suspend fun PatchBundleEntity.load(dir: File) = when (source) {
        is SourceInfo.Local -> LocalPatchBundle(name, uid, dir)
        is SourceInfo.Remote -> RemoteBundle(
            name,
            uid,
            dir,
            if (uid != 0) source.url.toString() else prefs.api.get()
        )
    }

    suspend fun load() = withContext(Dispatchers.Default) {
        val entities = persistenceRepo.loadConfiguration().onEach {
            Log.d(tag, "Bundle: $it")
        }

        _sources.value = entities.associate {
            val dir = directoryOf(it.uid)
            val bundle = it.load(dir)

            it.uid to bundle
        }
    }

    suspend fun resetConfig() = withContext(Dispatchers.Default) {
        persistenceRepo.reset()
        _sources.value = emptyMap()
        bundlesDir.apply {
            deleteRecursively()
            mkdirs()
        }

        load()
    }

    suspend fun remove(bundle: BundleSource) = withContext(Dispatchers.Default) {
        persistenceRepo.delete(bundle.uid)
        directoryOf(bundle.uid).deleteRecursively()

        _sources.update {
            it.filterValues { value ->
                value.uid != bundle.uid
            }
        }
    }

    private fun addBundle(patchBundle: BundleSource) =
        _sources.update { it.toMutableMap().apply { put(patchBundle.uid, patchBundle) } }

    suspend fun createLocal(name: String, patches: InputStream, integrations: InputStream?) {
        val id = persistenceRepo.create(name, SourceInfo.Local)
        val source = LocalPatchBundle(name, id, directoryOf(id))

        addBundle(source)

        source.replace(patches, integrations)
    }

    suspend fun createRemote(name: String, apiUrl: Url, autoUpdate: Boolean) {
        val id = persistenceRepo.create(name, SourceInfo.Remote(apiUrl), autoUpdate)
        addBundle(RemoteBundle(name, id, directoryOf(id), apiUrl.toString()))
    }

    private suspend fun getRemoteBundles() = sources.first().filterIsInstance<RemoteBundle>()

    suspend fun onApiUrlChange() {
        _sources.value[0]?.let { it as? RemoteBundle }?.deleteLocalFiles()
        load()
    }

    suspend fun redownloadRemoteBundles() = getRemoteBundles().forEach { it.downloadLatest() }

    suspend fun updateCheck() = supervisorScope {
        if (!networkInfo.isSafe()) {
            Log.d(tag, "Skipping update check because the network is unsafe.")
            return@supervisorScope
        }

        getRemoteBundles().forEach {
            launch {
                if (!it.propsFlow().first().autoUpdate) return@launch
                Log.d(tag, "Updating patch bundle: ${it.name}")
                it.update()
            }
        }
    }
}