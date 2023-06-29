package app.revanced.manager.domain.manager

import android.content.SharedPreferences
import app.revanced.manager.domain.manager.base.BasePreferenceManager
import app.revanced.manager.ui.theme.Theme

/**
 * @author Hyperion Authors, zt64
 */
class PreferencesManager(
    sharedPreferences: SharedPreferences
) : BasePreferenceManager(sharedPreferences) {
    var dynamicColor by booleanPreference("dynamic_color", true)
    var theme by enumPreference("theme", Theme.SYSTEM)
    //var sentry by booleanPreference("sentry", true)

    var preferSplits by booleanPreference("prefer_splits", false)
    var preferUniversal by booleanPreference("prefer_universal", false)

    var keystoreCommonName by stringPreference("keystore_cn", KeystoreManager.DEFAULT)
    var keystorePass by stringPreference("keystore_pass", KeystoreManager.DEFAULT)
}
