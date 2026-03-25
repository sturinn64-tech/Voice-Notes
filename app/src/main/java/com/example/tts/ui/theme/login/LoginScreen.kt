package com.example.tts.ui.theme.login

import android.R.attr.content
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tts.ui.theme.common.passwordTextField
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val auth = Firebase.auth
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    // ИСПРАВЛЕНО: разные состояния для каждой кнопки
    val isLoadingSignIn = remember { mutableStateOf(false) }
    val isLoadingSignUp = remember { mutableStateOf(false) }

    // Валидация
    val isPasswordValid = passwordState.value.length >= 6
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailState.value).matches()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Voice Notes", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Добро пожаловать!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text("Электронная почта") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailState.value.isNotEmpty() && !isEmailValid,
                supportingText = {
                    if (emailState.value.isNotEmpty() && !isEmailValid) {
                        Text("Некорректный формат email", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            passwordTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                label = "Пароль",
                modifier = Modifier.fillMaxWidth(),
                isError = passwordState.value.isNotEmpty() && !isPasswordValid,
                supportingText = {
                    if (passwordState.value.isNotEmpty() && !isPasswordValid) {
                        Text("Минимум 6 символов", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка Войти
            Button(
                onClick = {
                    if (isLoadingSignIn.value) return@Button // Защита от дубликатов
                    isLoadingSignIn.value = true
                    Log.d("LoginDebug", "Попытка входа: email=${emailState.value}")

                    signIn(auth, emailState.value, passwordState.value) { success, error ->
                        isLoadingSignIn.value = false
                        Log.d("LoginDebug", "Результат входа: success=$success, error=$error")

                        if (success) {
                            Log.d("MyLog", "Вход выполнен успешно!")
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(error ?: "Ошибка входа. Проверьте интернет и попробуйте снова.")
                            }
                        }
                    }
                },
                enabled = !isLoadingSignIn.value && isEmailValid && isPasswordValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoadingSignIn.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Войти")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка Зарегистрироваться
            Button(
                onClick = {
                    if (isLoadingSignUp.value) return@Button // Защита от дубликатов
                    isLoadingSignUp.value = true
                    Log.d("LoginDebug", "Попытка регистрации: email=${emailState.value}")

                    signUp(auth, emailState.value, passwordState.value) { success, error ->
                        isLoadingSignUp.value = false
                        Log.d("LoginDebug", "Результат регистрации: success=$success, error=$error")

                        if (success) {
                            Log.d("MyLog", "Регистрация успешна!")
                            // Автоматически логиним после регистрации
                            signIn(auth, emailState.value, passwordState.value) { signInSuccess, signInError ->
                                if (!signInSuccess) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Регистрация успешна, но вход не удался: ${signInError ?: "неизвестная ошибка"}")
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                val errorMessage = when {
                                    error?.contains("email already in use", ignoreCase = true) == true ->
                                        "Этот email уже зарегистрирован. Войдите с существующим паролем."
                                    else -> error ?: "Ошибка регистрации. Проверьте интернет и попробуйте снова."
                                }
                                snackbarHostState.showSnackbar(errorMessage)
                            }
                        }
                    }
                },
                enabled = !isLoadingSignUp.value && isEmailValid && isPasswordValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                if (isLoadingSignUp.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Text("Зарегистрироваться")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Подсказки
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Советы:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Пароль скрывается при вводе",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Минимальная длина пароля — 6 символов",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Убедитесь, что есть интернет-соединение",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Регистрация
private fun signUp(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onComplete: (Boolean, String?) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            Log.d("FirebaseDebug", "Регистрация завершена: success=${task.isSuccessful}, error=${task.exception?.message}")
            onComplete(task.isSuccessful, task.exception?.message)
        }
        .addOnFailureListener { exception ->
            Log.e("FirebaseDebug", "Ошибка регистрации: ${exception.message}", exception)
            onComplete(false, exception.message)
        }
}

// Вход
private fun signIn(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onComplete: (Boolean, String?) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            Log.d("FirebaseDebug", "Вход завершён: success=${task.isSuccessful}, error=${task.exception?.message}")
            onComplete(task.isSuccessful, task.exception?.message)
        }
        .addOnFailureListener { exception ->
            Log.e("FirebaseDebug", "Ошибка входа: ${exception.message}", exception)
            onComplete(false, exception.message)
        }
}