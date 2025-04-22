package com.example.a2048.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.a2048.data.model.User
import com.example.a2048.data.repository.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    var leaderboardState by remember { mutableStateOf<Result<List<User>>>(Result.Loading) }
    var currentUserId by remember { mutableStateOf("") }
    
    LaunchedEffect(true) {
        currentUserId = viewModel.getCurrentUserId() ?: ""
        leaderboardState = viewModel.getLeaderboard()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Таблица лидеров") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (leaderboardState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }
                is Result.Success -> {
                    val users = (leaderboardState as Result.Success<List<User>>).data
                    
                    if (users.isEmpty()) {
                        Text(
                            text = "Пока нет результатов. Станьте первым!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Место",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.2f)
                                )
                                Text(
                                    text = "Игрок",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.5f)
                                )
                                Text(
                                    text = "Счёт",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.3f),
                                    textAlign = TextAlign.End
                                )
                            }
                            
                            Divider()
                            
                            // List of players
                            LazyColumn {
                                itemsIndexed(users) { index, user ->
                                    val isCurrentUser = user.id == currentUserId
                                    val rowBackgroundColor = if (isCurrentUser) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(rowBackgroundColor)
                                            .padding(vertical = 12.dp, horizontal = 16.dp)
                                    ) {
                                        // Rank
                                        Text(
                                            text = "#${index + 1}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(0.2f)
                                        )
                                        
                                        // Player name - use username if available, otherwise email
                                        Text(
                                            text = if (user.username.isNotBlank()) user.username else user.email,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(0.5f)
                                        )
                                        
                                        // Score
                                        Text(
                                            text = user.highScore.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(0.3f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    
                                    Divider()
                                }
                            }
                        }
                    }
                }
                is Result.Error -> {
                    Text(
                        text = "Не удалось загрузить таблицу лидеров: ${(leaderboardState as Result.Error).exception.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
} 