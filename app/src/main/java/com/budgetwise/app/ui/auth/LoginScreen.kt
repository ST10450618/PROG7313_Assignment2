package com.budgetwise.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
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
import com.budgetwise.app.ui.theme.TealPrimary

/**
 * Login screen — first screen shown to unauthenticated users.
 *
 * Features:
 * - Username text field with keyboard "Next" action
 * - Password field with show/hide toggle icon
 * - Error text below password field (visible when errorMsg is non-null)
 * - Loading button (shows CircularProgressIndicator while login is in progress)
 * - "Don't have an account? Register" link at the bottom
 * - Keyboard navigation: username → password → submit on IME Done
 */
@Composable
fun LoginScreen(
    onLoginSuccess:        () -> Unit,
    onNavigateToRegister:  () -> Unit,
    viewModel:             AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate on success (side effect — only fires when isSuccess becomes true)
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }

    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager    = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App title / logo area
        Text(
            text  = "💰 BudgetWise",
            style = MaterialTheme.typography.headlineLarge,
            color = TealPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text  = "Smart Financial Management",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Username field
        OutlinedTextField(
            value         = username,
            onValueChange = { username = it; viewModel.clearError() },
            label         = { Text("Username") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction    = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = TealPrimary,
                focusedLabelColor    = TealPrimary,
                cursorColor          = TealPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field with visibility toggle
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
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
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
                    viewModel.login(username, password)
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
                text  = uiState.errorMsg!!,
                color = CoralAlert,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login button (shows progress indicator while loading)
        Button(
            onClick  = { viewModel.login(username, password) },
            enabled  = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color    = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Log In", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register link
        TextButton(onClick = onNavigateToRegister) {
            Text(
                text  = "Don't have an account? Register",
                color = TealPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
