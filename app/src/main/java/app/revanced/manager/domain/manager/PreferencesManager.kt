package app.revanced.manager.domain.manager

import android.content.Context
import app.revanced.manager.R
import app.revanced.manager.domain.manager.base.BasePreferencesManager
import app.revanced.manager.ui.theme.Theme

class PreferencesManager(
    context: Context
) : BasePreferencesManager(context, "settings") {

    enum class InstallerManager(val displayName: Int) {
        DEFAULT(R.string.default_installer),
        SHIZUKU(R.string.shizuku_installer),
        ROOT(R.string.root_installer),
        MAGISK(R.string.magisk_installer),
    }

    val dynamicColor = booleanPreference("dynamic_color", true)
    val theme = enumPreference("theme", Theme.SYSTEM)

    val api = stringPreference("api_url", "https://api.revanced.app")

    val allowExperimental = booleanPreference("allow_experimental", false)

    val keystoreCommonName = stringPreference("keystore_cn", KeystoreManager.DEFAULT)
    val keystorePass = stringPreference("keystore_pass", KeystoreManager.DEFAULT)

    val preferSplits = booleanPreference("prefer_splits", false)
    val defaultInstaller = enumPreference("installer", InstallerManager.DEFAULT)

    val showAutoUpdatesDialog = booleanPreference("show_auto_updates_dialog", true)
    val managerAutoUpdates = booleanPreference("manager_auto_updates", false)
}
