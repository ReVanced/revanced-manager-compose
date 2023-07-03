package app.revanced.manager.util.saver

import androidx.compose.runtime.saveable.Saver
import java.nio.file.Path
import kotlin.io.path.Path

val PathSaver = Saver<Path, String>(
    save = { it.toString() },
    restore = { Path(it) }
)