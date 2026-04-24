package com.example.tts

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tts.data.settings.AppSettings
import com.example.tts.navigation.AppScreen
import com.example.tts.navigation.bottomBarScreens
import com.example.tts.ui.theme.login.LoginScreen
import com.example.tts.ui.theme.main.HistoryScreen
import com.example.tts.ui.theme.main.HistoryViewModel
import com.example.tts.ui.theme.main.MainScreen
import com.example.tts.ui.theme.main.MainViewModel
import com.example.tts.ui.theme.settings.SettingsScreen
import com.example.tts.ui.theme.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun AppNavigation(
    appSettings: AppSettings,
    settingsViewModel: SettingsViewModel
) {
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (currentUser == null) {
        LoginScreen()
    } else {
        AuthorizedApp(
            user = currentUser!!,
            onSignOut = {
                auth.signOut()
                currentUser = null
            },
            appSettings = appSettings,
            settingsViewModel = settingsViewModel
        )
    }
}

@Composable
private fun AuthorizedApp(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    appSettings: AppSettings,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.provideFactory(application)
    )

    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModel.provideFactory(application)
    )

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Record.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AppScreen.Record.route) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                val mainUiState by mainViewModel.uiState.collectAsState()

                MainScreen(
                    uiState = mainUiState,
                    hasRecordPermission = hasPermission,
                    onSaveRecording = { filePath ->
                        mainViewModel.saveRecording(
                            filePath = filePath,
                            userId = user.uid
                        )
                    },
                    onClearError = mainViewModel::clearError,
                    onClearInfo = mainViewModel::clearInfo
                )
            }

            composable(AppScreen.History.route) {
                LaunchedEffect(user.uid) {
                    historyViewModel.loadMessages(user.uid)
                }

                val historyUiState by historyViewModel.uiState.collectAsState()

                HistoryScreen(
                    uiState = historyUiState,
                    onSearchQueryChange = historyViewModel::updateSearchQuery,
                    onSortChange = historyViewModel::updateSortOption,
                    onToggleFavoritesOnly = historyViewModel::toggleFavoritesOnly,
                    onPlayClick = historyViewModel::playOrStop,
                    onDeleteClick = historyViewModel::deleteRecording,
                    onFavoriteClick = historyViewModel::toggleFavorite,
                    onUpdateMessage = historyViewModel::updateMessage,
                    onRetryTranscription = historyViewModel::retryTranscription,
                    onExportTextClick = historyViewModel::exportText,
                    onExportAudioClick = historyViewModel::exportAudio,
                    onClearError = historyViewModel::clearError,
                    onClearInfo = historyViewModel::clearInfo
                )
            }

            composable(AppScreen.Settings.route) {
                SettingsScreen(
                    userEmail = user.email.orEmpty(),
                    themeMode = appSettings.themeMode,
                    confirmDelete = appSettings.confirmDelete,
                    defaultSort = appSettings.defaultHistorySort,
                    appVersion = "1.0",
                    onThemeModeChange = { settingsViewModel.updateThemeMode(it) },
                    onConfirmDeleteChange = { settingsViewModel.updateConfirmDelete(it) },
                    onDefaultSortChange = { settingsViewModel.updateDefaultHistorySort(it) },
                    onSignOut = onSignOut
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomBarScreens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) }
            )
        }
    }
}