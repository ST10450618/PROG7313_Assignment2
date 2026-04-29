package com.budgetwise.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.repository.UserRepository
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for authentication screens (Login and Register share this ViewModel).
 *
 * @param isLoading     True while the login/register coroutine is running (shows button spinner).
 * @param errorMsg      Non-null when validation fails or credentials are wrong.
 * @param isSuccess     True after successful login/register — triggers navigation in the screen.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMsg:  String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel for LoginScreen and RegisterScreen.
 *
 * Validates inputs, calls UserRepository, saves session on success.
 * Both screens observe the same [uiState] flow.
 *
 * Injected by Hilt (@HiltViewModel + @Inject constructor).
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /**
     * Attempt to log in with the given credentials.
     *
     * Validations (in order):
     *   1. Username must not be blank.
     *   2. Password must not be blank.
     *
     * On success: saves session via SessionManager, sets isSuccess=true.
     * On failure: sets errorMsg with an appropriate message.
     */
    fun login(username: String, password: String) {
        // Validation 1
        if (username.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Username is required") }
            return
        }
        // Validation 2
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Password is required") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMsg = null) }

        viewModelScope.launch {
            val user = userRepository.login(username.trim(), password)
            if (user != null) {
                sessionManager.save(user.id, user.username)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMsg = "Invalid username or password")
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    /**
     * Attempt to register a new account.
     *
     * Validations (in order):
     *   1. Username must not be blank.
     *   2. Username must be at least 3 characters.
     *   3. Password must not be blank.
     *   4. Password must be at least 6 characters.
     *   5. Password and confirm password must match.
     *
     * On success: saves session via SessionManager, sets isSuccess=true.
     * On failure: sets errorMsg with the first failing validation message.
     */
    fun register(username: String, password: String, confirmPassword: String) {
        // Validation 1
        if (username.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Username is required") }
            return
        }
        // Validation 2
        if (username.trim().length < 3) {
            _uiState.update { it.copy(errorMsg = "Username must be at least 3 characters") }
            return
        }
        // Validation 3
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Password is required") }
            return
        }
        // Validation 4
        if (password.length < 6) {
            _uiState.update { it.copy(errorMsg = "Password must be at least 6 characters") }
            return
        }
        // Validation 5
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMsg = "Passwords do not match") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMsg = null) }

        viewModelScope.launch {
            val userId = userRepository.register(username.trim(), password)
            if (userId > 0L) {
                sessionManager.save(userId, username.trim())
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMsg = "Username already taken. Please choose another.")
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /** Clear the current error message (called when user starts typing again). */
    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }
}
