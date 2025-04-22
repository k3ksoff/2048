package com.example.a2048.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.a2048.data.model.User
import com.example.a2048.ui.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetScoreDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    
    // Check if the current user is admin
    LaunchedEffect(Unit) {
        val isAdmin = viewModel.checkAdminAccess()
        if (!isAdmin) {
            // If not admin, navigate back to game screen
            navController.navigate(Routes.GAME) {
                popUpTo(Routes.ADMIN) { inclusive = true }
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Требуется доступ администратора")
            }
        } else {
            // Load users
            viewModel.loadUsers()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Админ-панель") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ошибка: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUsers() }) {
                            Text("Повторить")
                        }
                    }
                }
                uiState.users.isEmpty() -> {
                    Text("Пользователи не найдены")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.users) { user ->
                            UserItem(
                                user = user,
                                onDeleteClick = {
                                    selectedUser = user
                                    showDeleteDialog = true
                                },
                                onResetScoreClick = {
                                    selectedUser = user
                                    showResetScoreDialog = true
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
    
    // Delete user dialog
    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить пользователя") },
            text = { Text("Вы уверены, что хотите удалить ${selectedUser?.email}? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.deleteUser(selectedUser!!.id)
                            if (result) {
                                snackbarHostState.showSnackbar("Пользователь успешно удалён")
                            } else {
                                snackbarHostState.showSnackbar("Не удалось удалить пользователя")
                            }
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Reset score dialog
    if (showResetScoreDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showResetScoreDialog = false },
            title = { Text("Сбросить счёт") },
            text = { Text("Вы уверены, что хотите сбросить счёт для ${selectedUser?.email}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.resetUserScore(selectedUser!!.id)
                            if (result) {
                                snackbarHostState.showSnackbar("Счёт успешно сброшен")
                            } else {
                                snackbarHostState.showSnackbar("Не удалось сбросить счёт")
                            }
                        }
                        showResetScoreDialog = false
                    }
                ) {
                    Text("Сбросить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetScoreDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun UserItem(
    user: User,
    onDeleteClick: () -> Unit,
    onResetScoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User email and role
            Text(
                text = user.email,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Username display
            if (user.username.isNotBlank()) {
                Text(
                    text = "Имя пользователя: ${user.username}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = "Роль: ${user.role}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Счёт: ${user.highScore}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onResetScoreClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Сбросить счёт"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Сбросить счёт")
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить пользователя"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
} 