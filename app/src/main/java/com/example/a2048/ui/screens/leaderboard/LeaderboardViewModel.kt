package com.example.a2048.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import com.example.a2048.data.model.User
import com.example.a2048.data.repository.Result
import com.example.a2048.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    /**
     * Get the current user ID
     */
    fun getCurrentUserId(): String? {
        return userRepository.getCurrentUser()?.uid
    }
    
    /**
     * Fetch the leaderboard data
     */
    suspend fun getLeaderboard(limit: Int = 100): Result<List<User>> {
        return userRepository.getTopPlayers(limit)
    }
} 