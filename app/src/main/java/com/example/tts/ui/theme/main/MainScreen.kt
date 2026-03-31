package com.example.tts.ui.theme.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: FirebaseUser,
    hasRecordPermission: Boolean,
    onSignOut: () -> Unit,
    onSaveRecording: (filePath: String) -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasRecordPermission) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(hasRecordPermission) {
        permissionGranted = hasRecordPermission
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Запись") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Выйти"
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Текущий пользователь",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.email ?: user.uid,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (!permissionGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Для записи нужен доступ к микрофону.",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!granted) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    permissionGranted = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Разрешить доступ")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Новая голосовая заметка",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Запись сохранится локально и сразу попадет в историю.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        AudioRecorder(
                            onSaveRecording = onSaveRecording
                        )
                    }
                }
            }
        }
    }
}