package com.example.tts

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LaunchedEffectWithAuthListener(
    auth: FirebaseAuth,
    onAuthStateChanged: (com.google.firebase.auth.FirebaseUser?) -> Unit
) {
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            scope.launch {
                onAuthStateChanged(firebaseAuth.currentUser)
            }
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }
}