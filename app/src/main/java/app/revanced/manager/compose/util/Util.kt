package app.revanced.manager.compose.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import java.util.Locale

const val APK_MIMETYPE = "application/vnd.android.package-archive"

fun Context.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

fun Context.loadIcon(string: String): Drawable? {
    return try {
        packageManager.getApplicationIcon(string)
    } catch (e: NameNotFoundException) {
        null
    }
}

fun Context.toast(string: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, string, duration).show()
}

val Color.hexCode: String
    inline get() {
        val a: Int = (alpha * 255).toInt()
        val r: Int = (red * 255).toInt()
        val g: Int = (green * 255).toInt()
        val b: Int = (blue * 255).toInt()
        return java.lang.String.format(Locale.getDefault(), "%02X%02X%02X%02X", r, g, b, a)
    }