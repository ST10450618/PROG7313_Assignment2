package com.budgetwise.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.BackgroundLight
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.GreenSecondary
import com.budgetwise.app.ui.theme.TealPrimary
import com.budgetwise.app.ui.theme.YellowHighlight

/**
 * Calculate password strength as an integer from 0–4.
 *
 * Scoring criteria (1 point each):
 *   1. Length ≥ 8 characters
 *   2. Contains at least one uppercase letter
 *   3. Contains at least one digit
 *   4. Contains at least one special character
 *
 * @return Strength score 0 (very weak) to 4 (strong).
 */
fun calculatePasswordStrength(password: String): Int {
    if (password.isEmpty()) return 0
    var strength = 0
    if (password.length >= 8)                         strength++
    if (password.any { it.isUpperCase() })            strength++
    if (password.any { it.isDigit() })                strength++
    if (password.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) strength++
    return strength
}

/**
 * Registration screen — creates a new BudgetWise account.
 *
 * Features:
 * - Back button (← returns to Login)
 * - Username text field
 * - Password field with show/hide toggle + colour-coded strength indicator bar
 * - Confirm password field
 * - Error text (CoralAlert colour)
 * - Loading button
 * - Strength indicator: 0=red, 1–2=yellow, 3=teal, 4=green
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack:    () -> Unit,
    viewModel:         AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    var username         by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }
    val focusManager     = LocalFocusManager.current

    val passwordStrength = calculatePasswordStrength(password)
    val strengthLabels   = listOf("", "Weak", "Fair", "Good", "Strong")
    val strengthColors   = listOf(
        Color.Transparent, CoralAlert, YellowHighlight, TealPrimary, GreenSecondary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Username
            OutlinedTextField(
                value         = username,
                onValueChange = { username = it; viewModel.clearError() },
                label         = { Text("Username") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    focusedLabelColor  = TealPrimary,
                    cursorColor        = TealPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it; viewModel.clearError() },
                label         = { Text("Password") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                                          else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction    = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    focusedLabelColor  = TealPrimary,
                    cursorColor        = TealPrimary
                )
            )

            // Password strength indicator
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(4) { index ->
                        LinearProgressIndicator(
                            progress       = { if (index < passwordStrength) 1f else 0f },
                            modifier       = Modifier
                                .weight(1f)
                                .height(4.dp),
                            color          = strengthColors[passwordStrength],
                            trackColor     = Color.LightGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = strengthLabels[passwordStrength],
                    color = strengthColors[passwordStrength],
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm password
            OutlinedTextField(
                value         = confirmPassword,
                onValueChange = { confirmPassword = it; viewModel.clearError() },
                label         = { Text("Confirm Password") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Filled.Visibility
                                          else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction    = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.register(username, password, confirmPassword)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    focusedLabelColor  = TealPrimary,
                    cursorColor        = TealPrimary
                )
            )

            // Error message
            if (uiState.errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text      = uiState.errorMsg!!,
                    color     = CoralAlert,
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register button
            Button(
                onClick  = { viewModel.register(username, password, confirmPassword) },
                enabled  = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Account", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
