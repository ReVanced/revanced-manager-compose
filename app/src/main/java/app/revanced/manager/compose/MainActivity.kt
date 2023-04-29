package app.revanced.manager.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import app.revanced.manager.compose.domain.manager.PreferencesManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.revanced.manager.compose.destination.Destination
import app.revanced.manager.compose.destination.DashboardDestination
import app.revanced.manager.compose.ui.screen.DashboardPage
import app.revanced.manager.compose.ui.screen.HomeScreen
import app.revanced.manager.compose.ui.theme.ReVancedManagerTheme
import app.revanced.manager.compose.ui.theme.Theme
import dev.olshevski.navigation.reimagined.*
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
                        is DashboardDestination -> {
                            HomeScreen(
                                pages = DashboardPage.values()
                            )
                        }
                    }
                }
            }
        }
    }
}