package app.revanced.manager.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.revanced.manager.compose.destination.Destination
import app.revanced.manager.compose.domain.manager.PreferencesManager
import app.revanced.manager.compose.ui.component.settings.AboutScreen
import app.revanced.manager.compose.ui.component.settings.DownloaderSettings
import app.revanced.manager.compose.ui.component.settings.GeneralSettings
import app.revanced.manager.compose.ui.component.settings.ImportExportSettings
import app.revanced.manager.compose.ui.component.settings.SourcesSettings
import app.revanced.manager.compose.ui.component.settings.UpdatesSettings
import app.revanced.manager.compose.ui.screen.DashboardScreen
import app.revanced.manager.compose.ui.screen.SettingsScreen
import app.revanced.manager.compose.ui.theme.ReVancedManagerTheme
import app.revanced.manager.compose.ui.theme.Theme
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val prefs: PreferencesManager by inject()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        setContent {
            ReVancedManagerTheme(
                darkTheme = prefs.theme == Theme.SYSTEM && isSystemInDarkTheme() || prefs.theme == Theme.DARK,
                dynamicColor = prefs.dynamicColor
            ) {
                val navController = rememberNavController<Destination>(startDestination = Destination.Dashboard)

                NavBackHandler(navController)

                AnimatedNavHost(
                    controller = navController,
                ) { destination ->
                    when (destination) {
                        is Destination.Dashboard -> {
                            DashboardScreen(onSettingsClick = {
                                navController.navigate(Destination.Settings)
                            })
                        }

                        is Destination.Settings -> {
                            SettingsScreen(
                                onBackClick = { navController.pop() },
                                navigate = { navController.navigate(it) }
                            )
                        }

                        is Destination.GeneralSettings -> {
                            GeneralSettings(
                                onBackClick = { navController.pop() },
                                preferences = prefs
                            )
                        }

                        is Destination.UpdatesSettings -> {
                            UpdatesSettings(
                                onBackClick = { navController.pop() },
                                preferences = prefs
                            )
                        }

                        is Destination.SourcesSettings -> {
                            SourcesSettings(
                                onBackClick = { navController.pop() },
                                preferences = prefs
                            )
                        }

                        is Destination.DownloaderSettings -> {
                            DownloaderSettings(
                                onBackClick = { navController.pop() },
                                preferences = prefs
                            )
                        }

                        is Destination.ImportExportSettings -> {
                            ImportExportSettings(
                                onBackClick = { navController.pop() },
                                preferences = prefs
                            )
                        }

                        is Destination.About -> {
                            AboutScreen(
                                onBackClick = { navController.pop() },
                                preferences = prefs
                            )
                        }
                    }
                }
            }
        }
    }
}