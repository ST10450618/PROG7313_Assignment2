package com.budgetwise.app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.repository.UserRepository
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error    : String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val session : SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank()) { _state.value = AuthState(error = "Username is required"); return }
        if (password.isBlank()) { _state.value = AuthState(error = "Password is required"); return }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            Log.d(TAG, "Login attempt: $username")
            val user = userRepo.login(username.trim(), password)
            if (user != null) {
                session.save(user.id, user.username)
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = "Incorrect username or password")
            }
        }
    }

    fun register(username: String, password: String, confirm: String) {
        when {
            username.isBlank()       -> { _state.value = AuthState(error = "Username is required"); return }
            username.length < 4      -> { _state.value = AuthState(error = "Username must be at least 4 characters"); return }
            password.length < 6      -> { _state.value = AuthState(error = "Password must be at least 6 characters"); return }
            password != confirm      -> { _state.value = AuthState(error = "Passwords do not match"); return }
        }
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            Log.d(TAG, "Register attempt: $username")
            val id = userRepo.register(username.trim(), password)
            if (id > 0) {
                session.save(id, username.trim())
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = "Username already taken — please choose another")
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}