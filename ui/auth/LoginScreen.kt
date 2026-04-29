package com.budgetwise.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.TealPrimary

@Composable
fun LoginScreen(
    onLoginSuccess      : () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel           : AuthViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsStateWithLifecycle()
    val focusManager  = LocalFocusManager.current
    var username     by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onLoginSuccess() }

    Column(
        modifier              = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Icon(Icons.Filled.AccountBalanceWallet, null, tint = TealPrimary, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(8.dp))
        Text("BudgetWise", style = MaterialTheme.typography.headlineLarge, color = TealPrimary, fontWeight = FontWeight.Bold)
        Text("Smart Financial Management", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value         = username,
            onValueChange = { username = it; viewModel.clearError() },
            label         = { Text("Username") },
            leadingIcon   = { Icon(Icons.Filled.Person, null) },
            singleLine    = true,
            isError       = state.error != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier      = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value               = password,
            onValueChange       = { password = it; viewModel.clearError() },
            label               = { Text("Password") },
            leadingIcon         = { Icon(Icons.Filled.Lock, null) },
            trailingIcon        = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            },
            singleLine          = true,
            isError             = state.error != null,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions     = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions     = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.login(username, password) }),
            modifier            = Modifier.fillMaxWidth()
        )

        state.error?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = CoralAlert, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = !state.isLoading
        ) {
            if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            else Text("Log In", fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToRegister) { Text("Don't have an account? Register") }
    }
}