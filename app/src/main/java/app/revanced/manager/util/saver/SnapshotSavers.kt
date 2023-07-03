package app.revanced.manager.util.saver

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.toMutableStateMap
import app.revanced.manager.util.SnapshotStateSet
import app.revanced.manager.util.toMutableStateSet

fun <T> snapshotStateListSaver() = Saver<SnapshotStateList<T>, List<T>>(
    save = {
        it.toMutableList()
    },
    restore = {
        it.toMutableStateList()
    }
)

fun <K, V> snapshotStateMapSaver() = Saver<SnapshotStateMap<K, V>, Map<K, V>>(
    save = {
        it.toMutableMap()
    },
    restore = {
        mutableStateMapOf<K, V>().apply {
            this.putAll(it)
        }
    }
)

/**
 * Create a saver that can save [SnapshotStateMap]s.
 * Null values will not be saved by this saver.
 */
fun <K, Original, Saveable : Any> snapshotStateMapSaver(
    valueSaver: Saver<Original, Saveable>
) = Saver<SnapshotStateMap<K, Original>, Map<K, Saveable>>(
    save = {
        buildMap {
            it.forEach { (key, value) ->
                with(valueSaver) {
                    save(value)?.let {
                        this@buildMap[key] = it
                    }
                }
            }
        }
    },
    restore = {
        it.mapNotNull { (key, value) ->
            valueSaver.restore(value)?.let { restored -> key to restored }
        }.toMutableStateMap()
    }
)

fun <T> snapshotStateSetSaver() = Saver<SnapshotStateSet<T>, Set<T>>(
    save = {
        it.toMutableSet()
    },
    restore = {
        it.toMutableStateSet()
    }
)