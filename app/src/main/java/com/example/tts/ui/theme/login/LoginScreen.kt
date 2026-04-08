package com.example.tts.ui.theme.login

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

private enum class AuthMode(
    val title: String,
    val subtitle: String,
    val actionText: String,
    val switchPrefix: String,
    val switchAction: String
) {
    SIGN_IN(
        title = "С возвращением",
        subtitle = "Войди в аккаунт и продолжай работать с голосовыми заметками.",
        actionText = "Войти",
        switchPrefix = "Еще нет аккаунта?",
        switchAction = "Регистрация"
    ),
    SIGN_UP(
        title = "Создай аккаунт",
        subtitle = "Зарегистрируйся, чтобы сохранять и расшифровывать свои записи.",
        actionText = "Зарегистрироваться",
        switchPrefix = "Уже есть аккаунт?",
        switchAction = "Вход"
    )
}

@Composable
fun LoginScreen() {
    val auth = Firebase.auth
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var mode by rememberSaveable { mutableStateOf(AuthMode.SIGN_IN) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val isEmailValid = remember(email) {
        email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }
    val isPasswordValid = remember(password) {
        password.isBlank() || password.length >= 6
    }

    val canSubmit = email.trim().isNotBlank() &&
            password.isNotBlank() &&
            isEmailValid &&
            isPasswordValid &&
            !isLoading

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        AuthHeader(mode = mode)

                        ModeSwitcher(
                            selectedMode = mode,
                            onModeSelected = { mode = it }
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it.trim() },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                label = { Text("Email") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Email,
                                        contentDescription = null
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                isError = email.isNotBlank() && !isEmailValid,
                                supportingText = {
                                    if (email.isNotBlank() && !isEmailValid) {
                                        Text("Проверь формат email")
                                    }
                                },
                                colors = authTextFieldColors()
                            )

                            AuthPasswordField(
                                value = password,
                                onValueChange = { password = it },
                                isError = password.isNotBlank() && !isPasswordValid,
                                onDone = {
                                    if (canSubmit) {
                                        submitAuth(
                                            auth = auth,
                                            mode = mode,
                                            email = email.trim(),
                                            password = password,
                                            onLoadingChange = { isLoading = it },
                                            onError = { error ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(error)
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                        }

                        Button(
                            onClick = {
                                submitAuth(
                                    auth = auth,
                                    mode = mode,
                                    email = email.trim(),
                                    password = password,
                                    onLoadingChange = { isLoading = it },
                                    onError = { error ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(error)
                                        }
                                    }
                                )
                            },
                            enabled = canSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = mode.actionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mode.switchPrefix,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    mode = if (mode == AuthMode.SIGN_IN) {
                                        AuthMode.SIGN_UP
                                    } else {
                                        AuthMode.SIGN_IN
                                    }
                                }
                            ) {
                                Text(mode.switchAction)
                            }
                        }

                        TipsBlock()
                    }
                }
            }
        }
    }
}

private fun submitAuth(
    auth: FirebaseAuth,
    mode: AuthMode,
    email: String,
    password: String,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    if (email.isBlank() || password.isBlank()) return

    onLoadingChange(true)

    val callback: (Boolean, String?) -> Unit = { success, error ->
        onLoadingChange(false)
        if (!success) {
            onError(mapFirebaseError(error = error, mode = mode))
        }
    }

    when (mode) {
        AuthMode.SIGN_IN -> signIn(
            auth = auth,
            email = email,
            password = password,
            onComplete = callback
        )

        AuthMode.SIGN_UP -> signUp(
            auth = auth,
            email = email,
            password = password,
            onComplete = callback
        )
    }
}

private fun mapFirebaseError(error: String?, mode: AuthMode): String {
    val source = error.orEmpty()

    return when {
        source.contains("badly formatted", ignoreCase = true) ->
            "Некорректный email."

        source.contains("password is invalid", ignoreCase = true) ||
                source.contains("credential is incorrect", ignoreCase = true) ||
                source.contains("invalid credential", ignoreCase = true) ->
            "Неверный email или пароль."

        source.contains("no user record", ignoreCase = true) ||
                source.contains("user not found", ignoreCase = true) ->
            "Аккаунт с таким email не найден."

        source.contains("email address is already in use", ignoreCase = true) ||
                source.contains("email already in use", ignoreCase = true) ->
            "Этот email уже зарегистрирован."

        source.contains("weak-password", ignoreCase = true) ||
                source.contains("password should be at least", ignoreCase = true) ->
            "Пароль слишком короткий. Минимум 6 символов."

        source.contains("network error", ignoreCase = true) ->
            "Проблема с интернетом. Проверь соединение и попробуй снова."

        source.contains("too many requests", ignoreCase = true) ->
            "Слишком много попыток. Подожди немного и попробуй снова."

        mode == AuthMode.SIGN_IN ->
            "Не удалось войти. Проверь данные и попробуй снова."

        else ->
            "Не удалось зарегистрироваться. Попробуй снова."
    }
}

@Composable
private fun AuthHeader(mode: AuthMode) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Voice Notes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = mode.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = mode.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModeSwitcher(
    selectedMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AuthMode.entries.forEach { mode ->
            val selected = mode == selectedMode

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.outlineVariant
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (mode == AuthMode.SIGN_IN) "Вход" else "Регистрация",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    onDone: () -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        label = { Text("Пароль") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = if (visible) {
                        "Скрыть пароль"
                    } else {
                        "Показать пароль"
                    }
                )
            }
        },
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        isError = isError,
        supportingText = {
            if (isError) {
                Text("Минимум 6 символов")
            }
        },
        colors = authTextFieldColors()
    )
}

@Composable
private fun TipsBlock() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Что умеет экран",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "• Проверяет email и длину пароля",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• Позволяет быстро переключаться между входом и регистрацией",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• Автоматически реагирует на состояние Firebase Auth",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun authTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
)

private fun signUp(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onComplete: (Boolean, String?) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful, task.exception?.message)
        }
        .addOnFailureListener { exception ->
            onComplete(false, exception.message)
        }
}

private fun signIn(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onComplete: (Boolean, String?) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful, task.exception?.message)
        }
        .addOnFailureListener { exception ->
            onComplete(false, exception.message)
        }
}