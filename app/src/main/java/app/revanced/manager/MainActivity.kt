package app.revanced.manager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.revanced.manager.service.RootConnection
import app.revanced.manager.service.ManagerRootService
import app.revanced.manager.ui.component.AutoUpdatesDialog
import app.revanced.manager.ui.destination.Destination
import app.revanced.manager.ui.screen.AppInfoScreen
import app.revanced.manager.ui.screen.VersionSelectorScreen
import app.revanced.manager.ui.screen.AppSelectorScreen
import app.revanced.manager.ui.screen.DashboardScreen
import app.revanced.manager.ui.screen.InstallerScreen
import app.revanced.manager.ui.screen.PatchesSelectorScreen
import app.revanced.manager.ui.screen.SettingsScreen
import app.revanced.manager.ui.theme.ReVancedManagerTheme
import app.revanced.manager.ui.theme.Theme
import app.revanced.manager.ui.viewmodel.MainViewModel
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.Shell.FLAG_MOUNT_MASTER
import com.topjohnwu.superuser.internal.BuilderImpl
import com.topjohnwu.superuser.ipc.RootService
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.popUpTo
import dev.olshevski.navigation.reimagined.rememberNavController
import org.koin.androidx.compose.getViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.androidx.viewmodel.ext.android.getViewModel as getActivityViewModel

class MainActivity : ComponentActivity(), KoinComponent {
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val shellBuilder = BuilderImpl.create().setFlags(FLAG_MOUNT_MASTER)
        Shell.setDefaultBuilder(shellBuilder)

        val intent = Intent(this, ManagerRootService::class.java)
        RootService.bind(intent, get<RootConnection>())

        val vm: MainViewModel = getActivityViewModel()

        setContent {
            val theme by vm.prefs.theme.getAsState()
            val dynamicColor by vm.prefs.dynamicColor.getAsState()

            ReVancedManagerTheme(
                darkTheme = theme == Theme.SYSTEM && isSystemInDarkTheme() || theme == Theme.DARK,
                dynamicColor = dynamicColor
            ) {
                val navController =
                    rememberNavController<Destination>(startDestination = Destination.Dashboard)

                NavBackHandler(navController)

                val showAutoUpdatesDialog by vm.prefs.showAutoUpdatesDialog.getAsState()
                if (showAutoUpdatesDialog) {
                    AutoUpdatesDialog(vm::applyAutoUpdatePrefs)
                }

                AnimatedNavHost(
                    controller = navController
                ) { destination ->
                    when (destination) {
                        is Destination.Dashboard -> DashboardScreen(
                            onSettingsClick = { navController.navigate(Destination.Settings) },
                            onAppSelectorClick = { navController.navigate(Destination.AppSelector) },
                            onAppClick = { installedApp -> navController.navigate(Destination.ApplicationInfo(installedApp)) }
                        )

                        is Destination.ApplicationInfo -> AppInfoScreen(
                            onPatchClick = { packageName, patchesSelection ->
                                navController.navigate(Destination.VersionSelector(packageName, patchesSelection))
                            },
                            onBackClick = { navController.pop() },
                            viewModel = getViewModel { parametersOf(destination.installedApp) }
                        )

                        is Destination.Settings -> SettingsScreen(
                            onBackClick = { navController.pop() }
                        )

                        is Destination.AppSelector -> AppSelectorScreen(
                            onAppClick = { navController.navigate(Destination.VersionSelector(it)) },
                            onStorageClick = { navController.navigate(Destination.PatchesSelector(it)) },
                            onBackClick = { navController.pop() }
                        )

                        is Destination.VersionSelector -> VersionSelectorScreen(
                            onBackClick = { navController.pop() },
                            onAppClick = { selectedApp ->
                                navController.navigate(
                                    Destination.PatchesSelector(
                                        selectedApp,
                                        destination.patchesSelection
                                    )
                                )
                            },
                            viewModel = getViewModel { parametersOf(destination.packageName, destination.patchesSelection) }
                        )

                        is Destination.PatchesSelector -> PatchesSelectorScreen(
                            onBackClick = { navController.pop() },
                            onPatchClick = { patches, options ->
                                navController.navigate(
                                    Destination.Installer(
                                        destination.selectedApp,
                                        patches,
                                        options
                                    )
                                )
                            },
                            vm = getViewModel { parametersOf(destination) }
                        )

                        is Destination.Installer -> InstallerScreen(
                            onBackClick = { navController.popUpTo { it is Destination.Dashboard } },
                            vm = getViewModel { parametersOf(destination) }
                        )
                    }
                }
            }
        }
    }
}