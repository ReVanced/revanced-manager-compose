package app.revanced.manager.ui.component.patches

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.revanced.manager.patcher.patch.Option
import app.revanced.patcher.patch.PatchOption

/**
 * [Composable] functions do not support function references, so we have to use composable lambdas instead.
 */
private typealias OptionField = @Composable (Any?, (Any?) -> Unit) -> Unit

private val StringField: OptionField = { value, setValue ->
    val current = value as? String
    TextField(value = current ?: "", onValueChange = setValue)
}

private val BooleanField: OptionField = { value, setValue ->
    val current = value as? Boolean
    Switch(checked = current ?: false, onCheckedChange = setValue)
}

private val UnknownField: OptionField = { _, _ ->
    Text("This type has not been implemented")
}

@Composable
fun OptionField(option: Option, value: Any?, setValue: (Any?) -> Unit) {
    val implementation = remember(option.type) {
        when (option.type) {
            // These are the only two types that are currently used by the official patches.
            PatchOption.StringOption::class.java -> StringField
            PatchOption.BooleanOption::class.java -> BooleanField
            else -> UnknownField
        }
    }

    implementation(value, setValue)
}