package app.revanced.manager.compose.patcher

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.Patch

@Patch(false)
@Name("export-all-activities")
@Description("Makes all app activities exportable.")
@Version("0.0.1")
class ExportAllActivitiesPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file
            val activities = document.getElementsByTagName("activity")

            for(i in 0..activities.length) {
                activities.item(i)?.apply {
                    val exportedAttribute = attributes.getNamedItem(EXPORTED_FLAG)

                    if (exportedAttribute != null) {
                        if (exportedAttribute.nodeValue != "true")
                            exportedAttribute.nodeValue = "true"
                    }
                    // Reason why the attribute is added in the case it does not exist:
                    // https://github.com/revanced/revanced-patches/pull/1751/files#r1141481604
                    else document.createAttribute(EXPORTED_FLAG)
                        .apply { value = "true" }
                        .let(attributes::setNamedItem)
                }
            }
        }

        return PatchResultSuccess()
    }

    private companion object {
        const val EXPORTED_FLAG = "android:exported"
    }
}

val testingPatchBundle = listOf(ExportAllActivitiesPatch::class.java)

class PatcherState(val app: Application) {
    private val bundle = mutableStateOf(PatchBundle(testingPatchBundle))

    fun patchClassesFor(packageName: String, packageVersion: String) = bundle.value.getPatchesFiltered(packageName, packageVersion)
}