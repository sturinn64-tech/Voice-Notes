package com.example.tts.ui.theme.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: FirebaseUser,
    onGoToHistory: () -> Unit,
    hasRecordPermission: Boolean,
    onSignOut: () -> Unit,
    onSaveRecording: (fileName: String) -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasRecordPermission) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    // если permission в AppNavigation поменялась
    LaunchedEffect(hasRecordPermission) {
        permissionGranted = hasRecordPermission
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Main") },
                actions = {
                    IconButton(onClick = onGoToHistory) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.History,
                            contentDescription = "History"
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign out"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "User: ${user.email ?: user.uid}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!permissionGranted) {
                Text(
                    text = "Нужно разрешение на микрофон, иначе записывать нечем.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        else permissionGranted = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Разрешить микрофон")
                }
            } else {
                AudioRecorder(onSaveRecording = onSaveRecording)
            }
        }
    }
}