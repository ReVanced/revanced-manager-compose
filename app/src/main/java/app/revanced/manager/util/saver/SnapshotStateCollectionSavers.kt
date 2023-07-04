package app.revanced.manager.util.saver

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.toMutableStateMap
import app.revanced.manager.util.SnapshotStateSet
import app.revanced.manager.util.toMutableStateSet

/**
 * Create a saver for [SnapshotStateList]s.
 */
fun <T> snapshotStateListSaver() = Saver<SnapshotStateList<T>, List<T>>(
    save = {
        it.toMutableList()
    },
    restore = {
        it.toMutableStateList()
    }
)

/**
 * Create a saver for [SnapshotStateSet]s.
 */
fun <T> snapshotStateSetSaver() = Saver<SnapshotStateSet<T>, Set<T>>(
    save = {
        it.toMutableSet()
    },
    restore = {
        it.toMutableStateSet()
    }
)

/**
 * Create a saver for [SnapshotStateMap]s.
 */
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
 * Create a saver for [SnapshotStateMap]s with a custom saver used for the values.
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