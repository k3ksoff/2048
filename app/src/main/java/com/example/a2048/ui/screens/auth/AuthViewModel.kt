package com.example.a2048.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2048.data.repository.Result
import com.example.a2048.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for authentication screens
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object Success : AuthUiState()
}

/**
 * Authentication events
 */
sealed class AuthEvent {
    object NavigateToLogin : AuthEvent()
    object NavigateToRegister : AuthEvent()
    object NavigateToGame : AuthEvent()
}

/**
 * ViewModel for authentication screens
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // UI state
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState
    
    // Events
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events
    
    /**
     * Check if a user is already logged in
     */
    fun checkCurrentUser(): Boolean {
        return userRepository.getCurrentUser() != null
    }
    
    /**
     * Register a new user
     */
    fun register(email: String, password: String, confirmPassword: String, username: String = "") {
        // Validate inputs
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }
        
        if (!isValidEmail(email)) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }
        
        // Show loading state
        _uiState.value = AuthUiState.Loading
        
        // Register the user
        viewModelScope.launch {
            when (val result = userRepository.registerUser(email, password, username)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success
                    _events.emit(AuthEvent.NavigateToGame)
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState.Error(result.exception.message ?: "Registration failed")
                }
                else -> {
                    _uiState.value = AuthUiState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    /**
     * Login an existing user
     */
    fun login(email: String, password: String) {
        // Validate inputs
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }
        
        // Show loading state
        _uiState.value = AuthUiState.Loading
        
        // Login the user
        viewModelScope.launch {
            when (val result = userRepository.loginUser(email, password)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success
                    _events.emit(AuthEvent.NavigateToGame)
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState.Error(result.exception.message ?: "Login failed")
                }
                else -> {
                    _uiState.value = AuthUiState.Error("Unknown error occurred")
                }
            }
        }
    }
    
    /**
     * Navigate to login screen
     */
    fun navigateToLogin() {
        viewModelScope.launch {
            _events.emit(AuthEvent.NavigateToLogin)
        }
    }
    
    /**
     * Navigate to register screen
     */
    fun navigateToRegister() {
        viewModelScope.launch {
            _events.emit(AuthEvent.NavigateToRegister)
        }
    }
    
    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
} 