package com.example.a2048.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2048.data.game.Direction
import com.example.a2048.data.game.GameEngine
import com.example.a2048.data.game.GameState
import com.example.a2048.data.model.Tile
import com.example.a2048.data.repository.Result
import com.example.a2048.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the game screen
 */
data class GameUiState(
    val tiles: List<Tile> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val gameState: GameState = GameState.ONGOING,
    val isUserLoggedIn: Boolean = false,
    val isAdmin: Boolean = false
)

/**
 * Game events
 */
sealed class GameEvent {
    object NavigateToLeaderboard : GameEvent()
    object NavigateToLogin : GameEvent()
    object NavigateToAdmin : GameEvent()
    data class ShowMessage(val message: String) : GameEvent()
}

/**
 * ViewModel for the game screen
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Game engine
    private val gameEngine = GameEngine()
    
    // UI state
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    // Events
    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events
    
    init {
        newGame()
        checkLoggedInUser()
    }
    
    /**
     * Check if a user is logged in and update high score
     */
    private fun checkLoggedInUser() {
        val currentUser = userRepository.getCurrentUser()
        val isLoggedIn = currentUser != null
        
        _uiState.value = _uiState.value.copy(isUserLoggedIn = isLoggedIn)
        
        if (isLoggedIn) {
            viewModelScope.launch {
                when (val result = userRepository.getUserById(currentUser!!.uid)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            highScore = result.data.highScore,
                            isAdmin = result.data.isAdmin()
                        )
                    }
                    is Result.Error -> {
                        // Handle error silently, no need to show to user
                    }
                    else -> {}
                }
            }
        }
    }
    
    /**
     * Start a new game
     */
    fun newGame() {
        gameEngine.newGame()
        updateGameState()
    }
    
    /**
     * Process a swipe gesture
     */
    fun onSwipe(direction: Direction) {
        if (gameEngine.getState() == GameState.GAME_OVER) {
            return
        }
        
        val moved = gameEngine.moveTiles(direction)
        if (moved) {
            updateGameState()
            
            // Check for win condition
            if (gameEngine.getState() == GameState.WIN) {
                viewModelScope.launch {
                    _events.emit(GameEvent.ShowMessage("Congratulations! You've reached 2048! Continue playing to improve your score."))
                    gameEngine.continueAfterWin()
                }
            }
            // Check for game over condition
            else if (gameEngine.getState() == GameState.GAME_OVER) {
                viewModelScope.launch {
                    _events.emit(GameEvent.ShowMessage("Game Over! No more moves available."))
                    updateHighScore()
                }
            }
        }
    }
    
    /**
     * Update the high score if the current score is higher
     */
    private fun updateHighScore() {
        val currentUser = userRepository.getCurrentUser() ?: return
        val currentScore = gameEngine.getScore()
        
        if (currentScore > _uiState.value.highScore) {
            _uiState.value = _uiState.value.copy(highScore = currentScore)
            
            viewModelScope.launch {
                userRepository.updateHighScore(currentUser.uid, currentScore)
            }
        }
    }
    
    /**
     * Update the UI state with the current game state
     */
    private fun updateGameState() {
        _uiState.value = _uiState.value.copy(
            tiles = gameEngine.getTiles(),
            score = gameEngine.getScore(),
            gameState = gameEngine.getState()
        )
    }
    
    /**
     * Navigate to the leaderboard screen
     */
    fun navigateToLeaderboard() {
        viewModelScope.launch {
            _events.emit(GameEvent.NavigateToLeaderboard)
        }
    }
    
    /**
     * Logout the current user
     */
    fun logout() {
        userRepository.logout()
        _uiState.value = _uiState.value.copy(isUserLoggedIn = false, highScore = 0)
        
        viewModelScope.launch {
            _events.emit(GameEvent.NavigateToLogin)
        }
    }
    
    /**
     * Navigate to admin panel
     */
    fun navigateToAdmin() {
        viewModelScope.launch {
            _events.emit(GameEvent.NavigateToAdmin)
        }
    }
    
    /**
     * Continue game after reaching 2048
     */
    fun continueAfterWin() {
        gameEngine.continueAfterWin()
        updateGameState()
    }
} 