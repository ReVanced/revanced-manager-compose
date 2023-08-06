package app.revanced.manager.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.domain.bundles.PatchBundleSource
import app.revanced.manager.domain.bundles.PatchBundleSource.Companion.isDefault
import app.revanced.manager.ui.component.bundle.BundleItem
import app.revanced.manager.ui.viewmodel.BundlesViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun BundlesScreen(
    vm: BundlesViewModel = getViewModel(),
    sourcesSelectable: Boolean,
    editSelectedList: (PatchBundleSource) -> Unit,
    deleteSelectedSources: Boolean,
    onDeleteSelectedSources: () -> Unit,
    refreshSelectedSources: Boolean,
    onRefreshSelectedSources: () -> Unit,
    selectedSources: SnapshotStateList<PatchBundleSource>,
) {
    val sources by vm.sources.collectAsStateWithLifecycle(initialValue = emptyList())

    if (deleteSelectedSources) {
        selectedSources.forEach {
            if(!it.isDefault) {
                vm.delete(it)
            }
        }
        onDeleteSelectedSources()
    }

    if(refreshSelectedSources) {
        selectedSources.forEach {
            it.reload()
        }
        onRefreshSelectedSources()
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        sources.forEach {
            BundleItem(
                bundle = it,
                onDelete = {
                    vm.delete(it)
                },
                onUpdate = {
                    vm.update(it)
                },
                sourcesSelectable = sourcesSelectable,
                editSelectedList = {
                    editSelectedList(it)
                }
            )
        }
    }
}