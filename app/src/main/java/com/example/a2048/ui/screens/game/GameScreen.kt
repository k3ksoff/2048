package com.example.a2048.ui.screens.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.a2048.data.game.Direction
import com.example.a2048.data.game.GameState
import com.example.a2048.data.model.Tile
import com.example.a2048.ui.navigation.Routes
import com.example.a2048.ui.theme.GameBackground
import com.example.a2048.ui.theme.TileEmpty
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

// Composition Local для передачи состояния UI
val LocalGameUiState = compositionLocalOf<GameUiState> { 
    error("No GameUiState provided") 
}

// Composition Local для доступа к ViewModel
val LocalViewModel = compositionLocalOf<GameViewModel> {
    error("No GameViewModel provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Track if restart dialog is shown
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    
    // Handle events
    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is GameEvent.NavigateToLeaderboard -> {
                    navController.navigate(Routes.LEADERBOARD)
                }
                is GameEvent.NavigateToLogin -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                }
                is GameEvent.NavigateToAdmin -> {
                    navController.navigate(Routes.ADMIN)
                }
                is GameEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("2048") },
                actions = {
                    // Admin button (visible only for admins)
                    if (uiState.isAdmin) {
                        IconButton(onClick = { viewModel.navigateToAdmin() }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Админ-панель"
                            )
                        }
                    }
                
                    // Leaderboard button
                    IconButton(onClick = { viewModel.navigateToLeaderboard() }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Таблица лидеров"
                        )
                    }
                    
                    // Restart button
                    IconButton(onClick = { showRestartDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Новая игра"
                        )
                    }
                    
                    // Logout button (visible only when logged in)
                    if (uiState.isUserLoggedIn) {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Выйти"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalGameUiState provides uiState,
            LocalViewModel provides viewModel
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Score section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ScoreBox(title = "Счёт", score = uiState.score)
                        ScoreBox(title = "Лучшее", score = uiState.highScore)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Game board
                    GameBoard(
                        tiles = uiState.tiles,
                        onSwipe = { direction -> viewModel.onSwipe(direction) }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Game instructions
                    Text(
                        text = "Свайпайте для перемещения плиток.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Login button if not logged in
                    if (!uiState.isUserLoggedIn) {
                        Button(
                            onClick = { navController.navigate(Routes.LOGIN) }
                        ) {
                            Text("Войти для сохранения счёта")
                        }
                    }
                }
            }
        }
    }
    
    // Restart dialog
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Новая игра") },
            text = { Text("Вы уверены, что хотите начать новую игру? Ваш текущий прогресс будет утерян.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.newGame()
                        showRestartDialog = false
                    }
                ) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Нет")
                }
            }
        )
    }
    
    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выйти") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Нет")
                }
            }
        )
    }
}

@Composable
fun ScoreBox(title: String, score: Int) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(70.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GameBoard(
    tiles: List<Tile>,
    onSwipe: (Direction) -> Unit
) {
    var startX by remember { mutableStateOf(0f) }
    var startY by remember { mutableStateOf(0f) }
    var isSwipeEnabled by remember { mutableStateOf(true) }
    var swipeConsumed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // --- Анимация появления/исчезновения плиток ---
    var previousTiles by remember { mutableStateOf<List<Tile>>(emptyList()) }
    var disappearingTilesState by remember { mutableStateOf<List<Tile>>(emptyList()) }
    val currentIds = tiles.map { it.id }.toSet()
    val previousIds = previousTiles.map { it.id }.toSet()
    val appearingTiles = tiles.filter { it.id !in previousIds }
    val disappearingTiles = previousTiles.filter { it.id !in currentIds }
    val previousTileMap = previousTiles.associateBy { it.id }

    // Добавляем исчезающие плитки в состояние
    LaunchedEffect(disappearingTiles) {
        if (disappearingTiles.isNotEmpty()) {
            disappearingTilesState = disappearingTilesState + disappearingTiles
        }
    }
    // Удаляем исчезающие плитки после анимации
    fun removeDisappearingTile(id: Int) {
        disappearingTilesState = disappearingTilesState.filter { it.id != id }
    }
    // Сохраняем предыдущие плитки после каждого рендера
    LaunchedEffect(tiles) {
        previousTiles = tiles
    }
    // --- ---

    // Game over and win dialogs
    var showWinDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var currentScore by remember { mutableStateOf(0) }
    
    // Get the game state from the parent
    val uiState = LocalGameUiState.current
    val viewModel = LocalViewModel.current
    
    // Check for win/game over conditions
    LaunchedEffect(uiState.gameState, uiState.score) {
        currentScore = uiState.score
        
        when (uiState.gameState) {
            GameState.WIN -> {
                if (!showWinDialog) {
                    showWinDialog = true
                }
            }
            GameState.GAME_OVER -> {
                if (!showGameOverDialog) {
                    showGameOverDialog = true
                }
            }
            else -> { /* Game is ongoing */ }
        }
    }
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(GameBackground)
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (isSwipeEnabled) {
                            startX = offset.x
                            startY = offset.y
                            swipeConsumed = false
                        }
                    },
                    onDragEnd = {
                        swipeConsumed = false
                    },
                    onDragCancel = {
                        swipeConsumed = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!isSwipeEnabled || swipeConsumed) return@detectDragGestures
                        val endX = change.position.x
                        val endY = change.position.y
                        val deltaX = endX - startX
                        val deltaY = endY - startY
                        val minSwipeDistance = 80f
                        if (abs(deltaX) > minSwipeDistance || abs(deltaY) > minSwipeDistance) {
                            swipeConsumed = true
                            isSwipeEnabled = false
                            if (abs(deltaX) > abs(deltaY)) {
                                if (deltaX > 0) {
                                    onSwipe(Direction.RIGHT)
                                } else {
                                    onSwipe(Direction.LEFT)
                                }
                            } else {
                                if (deltaY > 0) {
                                    onSwipe(Direction.DOWN)
                                } else {
                                    onSwipe(Direction.UP)
                                }
                            }
                            startX = 0f
                            startY = 0f
                            coroutineScope.launch {
                                delay(300)
                                isSwipeEnabled = true
                            }
                        }
                    }
                )
            }
    ) {
        val gridSize = 4
        val tileSize = (maxWidth - 8.dp * (gridSize + 1)) / gridSize
        // Draw the empty board first (4x4 grid)
        for (row in 0 until gridSize) {
            for (column in 0 until gridSize) {
                EmptyTile(row = row, column = column, size = tileSize)
            }
        }
        // Draw disappearing tiles (анимация уменьшения)
        for (tile in disappearingTilesState) {
            DisappearingGameTile(tile = tile, size = tileSize) {
                removeDisappearingTile(tile.id)
            }
        }
        // Draw the tiles on top (анимация появления для новых и анимация перемещения)
        for (tile in tiles) {
            val isAppearing = tile.id in appearingTiles.map { it.id }
            val prev = previousTileMap[tile.id]
            val prevRow = prev?.row ?: tile.row
            val prevCol = prev?.column ?: tile.column
            GameTile(
                tile = tile,
                size = tileSize,
                animateAppear = isAppearing,
                prevRow = prevRow,
                prevCol = prevCol
            )
        }
    }
    
    // Win dialog
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = { Text("Поздравляем!", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = { 
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Вы достигли 2048!", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Счёт: $currentScore", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Вы можете продолжить игру, чтобы улучшить свой результат.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.continueAfterWin()
                        showWinDialog = false 
                    }
                ) {
                    Text("Продолжить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        viewModel.newGame()
                        showWinDialog = false 
                    }
                ) {
                    Text("Новая игра")
                }
            }
        )
    }
    
    // Game over dialog
    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { showGameOverDialog = false },
            title = { Text("Игра окончена!", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = { 
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Нет доступных ходов.", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Финальный счёт: $currentScore", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.newGame()
                        showGameOverDialog = false
                    }
                ) {
                    Text("Новая игра")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showGameOverDialog = false
                    }
                ) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
fun EmptyTile(row: Int, column: Int, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .offset(
                x = (size + 8.dp) * column + 4.dp,
                y = (size + 8.dp) * row + 4.dp
            )
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(TileEmpty)
    )
}

@Composable
fun GameTile(
    tile: Tile,
    size: androidx.compose.ui.unit.Dp,
    animateAppear: Boolean = false,
    prevRow: Int = tile.row,
    prevCol: Int = tile.column
) {
    val scale = remember { Animatable(if (animateAppear) 0.8f else 1f) }
    // Animate new tiles with a scale effect
    LaunchedEffect(key1 = tile.id) {
        if (animateAppear) {
            scale.animateTo(1.1f, animationSpec = tween(120))
            scale.animateTo(1f, animationSpec = tween(80))
        } else {
            scale.animateTo(1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ))
        }
    }
    // Animate merged tiles with a special effect
    val mergeScale = remember { Animatable(1f) }
    LaunchedEffect(key1 = tile.merged) {
        if (tile.merged) {
            mergeScale.animateTo(1.2f, animationSpec = tween(durationMillis = 100))
            mergeScale.animateTo(1f, animationSpec = tween(durationMillis = 100))
        }
    }
    Box(
        modifier = Modifier
            .offset(
                x = (size + 8.dp) * tile.column + 4.dp,
                y = (size + 8.dp) * tile.row + 4.dp
            )
            .size(size)
            .scale(scale.value * mergeScale.value)
            .clip(RoundedCornerShape(4.dp))
            .background(tile.getColor())
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            color = tile.getTextColor(),
            fontWeight = FontWeight.Bold,
            fontSize = tile.getFontSize().sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DisappearingGameTile(tile: Tile, size: androidx.compose.ui.unit.Dp, onAnimationEnd: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        finishedListener = { onAnimationEnd() }
    )
    LaunchedEffect(Unit) {
        visible = false
    }
    if (scale > 0f) {
        Box(
            modifier = Modifier
                .offset(
                    x = (size + 8.dp) * tile.column + 4.dp,
                    y = (size + 8.dp) * tile.row + 4.dp
                )
                .size(size)
                .scale(scale)
                .clip(RoundedCornerShape(4.dp))
                .background(tile.getColor())
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tile.value.toString(),
                color = tile.getTextColor(),
                fontWeight = FontWeight.Bold,
                fontSize = tile.getFontSize().sp,
                textAlign = TextAlign.Center
            )
        }
    }
} 