package com.example.a2048.data.model

/**
 * Represents a user in the application.
 * @param id Unique identifier for the user (typically UID from Firebase Auth)
 * @param email User's email address
 * @param username User's display name for the leaderboard
 * @param highScore User's highest score in the game
 * @param role User's role ("user" or "admin")
 */
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val highScore: Int = 0,
    val role: String = "user"
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ADMIN = "admin"
    }
    
    fun isAdmin(): Boolean = role == ROLE_ADMIN
} 