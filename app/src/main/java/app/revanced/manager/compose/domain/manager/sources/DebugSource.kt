package app.revanced.manager.compose.domain.manager.sources

import app.revanced.manager.compose.domain.manager.patch.PatchBundle
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

/**
 * This source is just used for testing and will be removed later.
 */
class DebugSource : Source(File("/dev/null")) {
    @Patch(false)
    @Name("i-always-succeed")
    class SuccessfulPatch : BytecodePatch() {
        override fun execute(context: BytecodeContext) = PatchResultSuccess()
    }

    @Patch(false)
    @Name("i-always-fail")
    class FailingPatch : BytecodePatch() {
        override fun execute(context: BytecodeContext) = PatchResultError("What did you expect?")
    }

    private companion object {
        val fakeBundle = PatchBundle(listOf(SuccessfulPatch::class.java, FailingPatch::class.java), null)
    }

    override val mutableBundle = MutableStateFlow(fakeBundle)
}