package com.example.tts

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tts.ui.theme.login.LoginScreen
import com.example.tts.ui.theme.main.MainScreen
import com.example.tts.ui.theme.main.HistoryScreen
import com.example.tts.ui.theme.main.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun AppNavigation() {
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (currentUser != null) {
        MainRoute(
            user = currentUser!!,
            onSignOut = {
                auth.signOut()
                currentUser = null
            }
        )
    } else {
        LoginScreen()
    }
}

@Composable
fun MainRoute(
    user: FirebaseUser,
    onSignOut: () -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    var currentScreen by remember { mutableStateOf("main") }
    val context = LocalContext.current

    LaunchedEffect(user.uid) {
        viewModel.loadMessages(userId = user.uid)
    }

    when (currentScreen) {
        "main" -> {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            MainScreen(
                user = user,
                onGoToHistory = { currentScreen = "history" },
                hasRecordPermission = hasPermission,
                onSignOut = onSignOut,
                onSaveRecording = { fileName ->
                    viewModel.saveRecording(fileName, user.uid, context)
                }
            )
        }

        "history" -> {
            HistoryScreen(
                uiState = viewModel.uiState.collectAsState().value,
                onBack = { currentScreen = "main" },
                onSignOut = onSignOut,
                viewModel = viewModel
            )
        }
    }
}