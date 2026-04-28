package com.budgetwise.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.TealPrimary

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack   : () -> Unit,
    viewModel        : AuthViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsStateWithLifecycle()
    val focusManager  = LocalFocusManager.current
    var username     by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirm      by remember { mutableStateOf("") }
    var showPass     by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onRegisterSuccess() }

    Column(
        modifier            = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Filled.AccountBalanceWallet, null, tint = TealPrimary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(8.dp))
        Text("Create Account", style = MaterialTheme.typography.headlineMedium, color = TealPrimary)
        Text("Join BudgetWise today", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = username, onValueChange = { username = it; viewModel.clearError() },
            label = { Text("Username") }, leadingIcon = { Icon(Icons.Filled.Person, null) },
            singleLine = true, isError = state.error != null,
            supportingText = { Text("4–20 characters") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it; viewModel.clearError() },
            label = { Text("Password") }, leadingIcon = { Icon(Icons.Filled.Lock, null) },
            trailingIcon = { IconButton(onClick = { showPass = !showPass }) {
                Icon(if (showPass) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null) } },
            singleLine = true, isError = state.error != null,
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            supportingText = { Text("Minimum 6 characters") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = confirm, onValueChange = { confirm = it; viewModel.clearError() },
            label = { Text("Confirm Password") }, leadingIcon = { Icon(Icons.Filled.Lock, null) },
            singleLine = true, isError = state.error != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.register(username, password, confirm) }),
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = CoralAlert, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.register(username, password, confirm) },
            modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !state.isLoading
        ) {
            if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            else Text("Create Account", fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateBack) { Text("Already have an account? Log In") }
    }
}