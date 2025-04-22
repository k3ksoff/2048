package com.example.a2048.data.repository

import com.example.a2048.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result wrapper class for API responses
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Repository for handling user-related operations
 */
@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Collection name in Firestore
    private val usersCollection = firestore.collection("users")
    
    /**
     * Register a new user
     */
    suspend fun registerUser(email: String, password: String, username: String = ""): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { firebaseUser ->
                // Create a user entry in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    username = username.ifEmpty { firebaseUser.email?.substringBefore('@') ?: "" },
                    highScore = 0,
                    role = User.ROLE_USER
                )
                
                // Save the user to Firestore
                usersCollection.document(firebaseUser.uid).set(user).await()
                
                Result.Success(firebaseUser)
            } ?: Result.Error(Exception("Failed to create user"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Login an existing user
     */
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.Success(firebaseUser)
            } else {
                Result.Error(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get the current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    /**
     * Get a user by ID from Firestore
     */
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get all users (for admin panel)
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Update a user's high score
     */
    suspend fun updateHighScore(userId: String, newScore: Int): Result<Boolean> {
        return try {
            // First get the current high score
            val userResult = getUserById(userId)
            if (userResult is Result.Success) {
                val currentHighScore = userResult.data.highScore
                
                // Only update if the new score is higher
                if (newScore > currentHighScore) {
                    usersCollection.document(userId)
                        .update("highScore", newScore)
                        .await()
                }
                Result.Success(true)
            } else {
                Result.Error(Exception("Failed to get user data"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Delete a user (admin function)
     */
    suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            // Delete from Firestore first
            usersCollection.document(userId).delete().await()
            
            // Delete from Firebase Auth
            val user = getCurrentUser()
            if (user != null && user.uid == userId) {
                user.delete().await()
            }
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Reset a user's high score (admin function)
     */
    suspend fun resetHighScore(userId: String): Result<Boolean> {
        return try {
            usersCollection.document(userId)
                .update("highScore", 0)
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get the top players for the leaderboard
     */
    suspend fun getTopPlayers(limit: Int = 100): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .orderBy("highScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Check if current user is an admin
     */
    suspend fun isCurrentUserAdmin(): Result<Boolean> {
        val currentUser = getCurrentUser() ?: return Result.Error(Exception("No user logged in"))
        
        return when (val result = getUserById(currentUser.uid)) {
            is Result.Success -> Result.Success(result.data.isAdmin())
            is Result.Error -> Result.Error(result.exception)
            else -> Result.Error(Exception("Unknown error"))
        }
    }
    
    /**
     * Logout the current user
     */
    fun logout() {
        firebaseAuth.signOut()
    }
} 