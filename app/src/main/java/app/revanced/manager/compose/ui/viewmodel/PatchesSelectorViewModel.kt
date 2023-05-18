package app.revanced.manager.compose.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.compose.patcher.data.repository.PatchesRepository
import app.revanced.manager.compose.patcher.patch.Option
import app.revanced.manager.compose.patcher.patch.PatchInfo
import app.revanced.manager.compose.util.PackageInfo
import kotlinx.coroutines.launch

class PatchesSelectorViewModel(val packageInfo: PackageInfo, private val patchesRepository: PatchesRepository) :
    ViewModel() {
    private val patchesList = listOf(
        FakePatch(
            "amogus-patch",
            "adds amogus to all apps, mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus mogus ",
            options = listOf(
                Option(
                    "amogus"
                )
            ),
            isSupported = true
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = false
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = false
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = true
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = false
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = false
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = false
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(
                Option(
                    "amogus"
                )
            ),
            isSupported = false
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = false
        ),
        FakePatch(
            "microg-support", "makes microg work",
            options = listOf(),
            isSupported = false
        ),

        ).let { it + it + it + it + it }

    private val testingList = patchesList.groupBy { if (it.isSupported) "supported" else "unsupported" }
        .mapValues { it.value.map { it.getReal() } }

    val bundles = mutableStateListOf(
        Bundle(
            name = "offical",
            patches = mapOf("supported" to emptyList(), "unsupported" to emptyList())
        ),
        Bundle(
            name = "extended",
            patches = testingList
        ),
        Bundle(
            name = "balls",
            patches = testingList
        ),
    )

    init {
        viewModelScope.launch {
            val realPatches =
                patchesRepository.patchClassesFor(packageInfo.packageName, packageInfo.version).map { PatchInfo(it) }
            bundles[0] = Bundle(
                name = "official",
                patches = mapOf("supported" to realPatches, "unsupported" to emptyList())
            )
        }
    }

    val selectedPatches = mutableStateListOf<PatchInfo>()

    data class Bundle(
        val name: String,
        val patches: Map<String, List<PatchInfo>>
    )

    data class FakePatch(
        val name: String,
        val description: String,
        val options: List<Option>,
        val isSupported: Boolean
    ) {
        fun getReal() = PatchInfo(
            name,
            description,
            null,
            false,
            null,
            options.takeIf { !it.isEmpty() }?.map { Option(name, name, "a", false) })
    }

    data class Option(
        val name: String
    )
}