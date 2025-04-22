package com.example.a2048.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2048.data.model.User
import com.example.a2048.data.repository.Result
import com.example.a2048.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the admin screen
 */
data class AdminUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the admin panel
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // UI state
    private val _uiState = MutableStateFlow(AdminUiState(isLoading = true))
    val uiState: StateFlow<AdminUiState> = _uiState
    
    /**
     * Check if the current user has admin access
     */
    suspend fun checkAdminAccess(): Boolean {
        return when (val result = userRepository.isCurrentUserAdmin()) {
            is Result.Success -> result.data
            else -> false
        }
    }
    
    /**
     * Load all users
     */
    fun loadUsers() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            when (val result = userRepository.getAllUsers()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        users = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Unknown error"
                    )
                }
            }
        }
    }
    
    /**
     * Delete a user
     */
    suspend fun deleteUser(userId: String): Boolean {
        return when (val result = userRepository.deleteUser(userId)) {
            is Result.Success -> {
                loadUsers() // Reload users after successful deletion
                true
            }
            else -> false
        }
    }
    
    /**
     * Reset a user's high score
     */
    suspend fun resetUserScore(userId: String): Boolean {
        return when (val result = userRepository.resetHighScore(userId)) {
            is Result.Success -> {
                loadUsers() // Reload users after successful reset
                true
            }
            else -> false
        }
    }
} 