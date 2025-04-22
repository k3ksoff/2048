package com.example.a2048.data.game

import com.example.a2048.data.model.Tile
import kotlin.random.Random
import android.util.Log

/**
 * Game direction enum for handling swipe actions
 */
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

/**
 * Game state enum for tracking game progress
 */
enum class GameState {
    ONGOING, WIN, GAME_OVER
}

/**
 * Main game engine for 2048
 */
class GameEngine(private val gridSize: Int = 4) {
    
    // Game state
    private var currentState = GameState.ONGOING
    
    // Game grid as 2D array of Tile objects
    private var grid: Array<Array<Int?>> = Array(gridSize) { Array<Int?>(gridSize) { null } }
    
    // List of all tiles currently on the board
    private var tiles = mutableListOf<Tile>()
    
    // Current score
    private var score = 0
    
    // Whether the game should continue after 2048 tile is achieved
    private var continueAfterWin = false
    
    // ID counter for tiles
    private var idCounter = 0
    
    // ID grid for animations
    private var idGrid: Array<Array<Int?>> = Array(gridSize) { Array<Int?>(gridSize) { null } }
    
    private val TAG = "GameEngine"
    
    /**
     * Initialize a new game
     */
    fun newGame() {
        Log.d(TAG, "--- newGame() ---")
        // Clear the grid and tiles
        grid = Array(gridSize) { Array<Int?>(gridSize) { null } }
        idGrid = Array(gridSize) { Array<Int?>(gridSize) { null } }
        tiles.clear()
        score = 0
        idCounter = 0
        currentState = GameState.ONGOING
        continueAfterWin = false
        Log.d(TAG, "Game reset. gridSize=$gridSize")
        
        // Add two initial tiles
        addRandomTile()
        addRandomTile()
        rebuildTiles()
    }
    
    /**
     * Add a random tile to the grid
     * 90% chance for a '2' tile, 10% chance for a '4' tile
     */
    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        
        // Find all empty cells
        for (row in 0 until gridSize) {
            for (column in 0 until gridSize) {
                if (grid[row][column] == null) {
                    emptyCells.add(Pair(row, column))
                }
            }
        }
        
        // If there are no empty cells, return
        if (emptyCells.isEmpty()) {
            Log.d(TAG, "No empty cells for new tile!")
            return
        }
        
        // Select a random empty cell
        val (row, column) = emptyCells.random()
        
        // Create a new tile (90% chance for 2, 10% chance for 4)
        val value = if (Random.nextDouble() < 0.9) 2 else 4
        grid[row][column] = value
        idGrid[row][column] = idCounter++
        Log.d(TAG, "Added tile $value at ($row, $column) with id ${idGrid[row][column]}")
    }
    
    /**
     * Move tiles in the specified direction
     */
    fun moveTiles(direction: Direction): Boolean {
        Log.d(TAG, "moveTiles($direction) called. Current state: $currentState")
        if (currentState == GameState.GAME_OVER) {
            Log.d(TAG, "moveTiles: GAME_OVER, move ignored.")
            return false
        }
        var moved = false
        when (direction) {
            Direction.LEFT -> {
                for (row in 0 until gridSize) {
                    val (newLine, newIds, lineMoved, lineScore) = mergeLine(
                        (0 until gridSize).map { grid[row][it] },
                        (0 until gridSize).map { idGrid[row][it] }
                    )
                    Log.d(TAG, "LEFT row $row: before=${(0 until gridSize).map { grid[row][it] }} after=$newLine moved=$lineMoved score+=$lineScore")
                    for (col in 0 until gridSize) {
                        grid[row][col] = newLine[col]
                        idGrid[row][col] = newIds[col]
                    }
                    if (lineMoved) moved = true
                    score += lineScore
                }
            }
            Direction.RIGHT -> {
                for (row in 0 until gridSize) {
                    val (newLine, newIds, lineMoved, lineScore) = mergeLine(
                        (gridSize - 1 downTo 0).map { grid[row][it] },
                        (gridSize - 1 downTo 0).map { idGrid[row][it] }
                    )
                    Log.d(TAG, "RIGHT row $row: before=${(gridSize - 1 downTo 0).map { grid[row][it] }} after=$newLine moved=$lineMoved score+=$lineScore")
                    for (i in 0 until gridSize) {
                        val col = gridSize - 1 - i
                        grid[row][col] = newLine[i]
                        idGrid[row][col] = newIds[i]
                    }
                    if (lineMoved) moved = true
                    score += lineScore
                }
            }
            Direction.UP -> {
                for (col in 0 until gridSize) {
                    val (newLine, newIds, lineMoved, lineScore) = mergeLine(
                        (0 until gridSize).map { grid[it][col] },
                        (0 until gridSize).map { idGrid[it][col] }
                    )
                    Log.d(TAG, "UP col $col: before=${(0 until gridSize).map { grid[it][col] }} after=$newLine moved=$lineMoved score+=$lineScore")
                    for (row in 0 until gridSize) {
                        grid[row][col] = newLine[row]
                        idGrid[row][col] = newIds[row]
                    }
                    if (lineMoved) moved = true
                    score += lineScore
                }
            }
            Direction.DOWN -> {
                for (col in 0 until gridSize) {
                    val (newLine, newIds, lineMoved, lineScore) = mergeLine(
                        (gridSize - 1 downTo 0).map { grid[it][col] },
                        (gridSize - 1 downTo 0).map { idGrid[it][col] }
                    )
                    Log.d(TAG, "DOWN col $col: before=${(gridSize - 1 downTo 0).map { grid[it][col] }} after=$newLine moved=$lineMoved score+=$lineScore")
                    for (i in 0 until gridSize) {
                        val row = gridSize - 1 - i
                        grid[row][col] = newLine[i]
                        idGrid[row][col] = newIds[i]
                    }
                    if (lineMoved) moved = true
                    score += lineScore
                }
            }
        }
        if (moved) {
            Log.d(TAG, "Move $direction successful, adding random tile and rebuilding tiles.")
            addRandomTile()
            rebuildTiles()
            updateGameState()
        } else {
            Log.d(TAG, "Move $direction did not change the board.")
        }
        return moved
    }
    
    /**
     * Классическая логика сдвига и слияния одной линии (ряда или столбца)
     * Возвращает новую линию, новые id, был ли сдвиг, и сколько очков начислить
     */
    private fun mergeLine(line: List<Int?>, ids: List<Int?>): Quad<List<Int?>, List<Int?>, Boolean, Int> {
        // 1. Сдвигаем все числа к началу (убираем null)
        val values: MutableList<Int?> = line.filterNotNull().toMutableList()
        val idList: MutableList<Int?> = ids.filterIndexed { i, _ -> line[i] != null }.toMutableList()
        var scoreAdd = 0
        var merged = BooleanArray(values.size) { false }
        var i = 0
        // 2. Проходим по линии и объединяем одинаковые соседние
        while (i < values.size - 1) {
            if (values[i] == values[i + 1] && !merged[i] && !merged[i + 1]) {
                values[i] = values[i]!! * 2
                scoreAdd += values[i]!!
                idList[i] = idCounter++ // Новый id для объединённой плитки
                values[i + 1] = null
                idList[i + 1] = null
                merged[i] = true
                i += 2
            } else {
                i++
            }
        }
        // 3. Сдвигаем снова (убираем null после слияний)
        val resultValues: MutableList<Int?> = values.filterNotNull().toMutableList()
        val resultIds: MutableList<Int?> = idList.filterIndexed { idx, _ -> values[idx] != null }.toMutableList()
        // 4. Заполняем null до нужной длины
        while (resultValues.size < gridSize) {
            resultValues.add(null)
            resultIds.add(null)
        }
        // Проверяем, было ли движение
        val moved = (0 until gridSize).any { line.getOrNull(it) != resultValues.getOrNull(it) }
        Log.d(TAG, "mergeLine: before=$line after=$resultValues moved=$moved scoreAdd=$scoreAdd")
        return Quad(resultValues, resultIds, moved, scoreAdd)
    }
    
    /**
     * Check if the game is in a win or game over state
     */
    private fun updateGameState() {
        if (currentState == GameState.GAME_OVER || (currentState == GameState.WIN && !continueAfterWin)) return
        if (tiles.any { it.value == 2048 }) {
            currentState = GameState.WIN
            Log.d(TAG, "GameState changed: WIN")
            return
        }
        if (tiles.size < gridSize * gridSize) {
            if (currentState != GameState.WIN) currentState = GameState.ONGOING
            return
        }
        // Проверяем возможные ходы
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val v = grid[row][col]
                if (v == null) continue
                if (row > 0 && grid[row - 1][col] == v) return
                if (row < gridSize - 1 && grid[row + 1][col] == v) return
                if (col > 0 && grid[row][col - 1] == v) return
                if (col < gridSize - 1 && grid[row][col + 1] == v) return
            }
        }
        currentState = GameState.GAME_OVER
        Log.d(TAG, "GameState changed: GAME_OVER")
    }
    
    /**
     * Allow the game to continue after reaching 2048
     */
    fun continueAfterWin() {
        if (currentState == GameState.WIN) {
            continueAfterWin = true
            currentState = GameState.ONGOING
        }
    }
    
    // Getters to expose game state data
    fun getGrid(): Array<Array<Tile?>> {
        val result = Array(gridSize) { arrayOfNulls<Tile>(gridSize) }
        for (tile in tiles) {
            result[tile.row][tile.column] = tile
        }
        return result
    }
    fun getTiles(): List<Tile> = tiles.toList()
    fun getScore(): Int = score
    fun getState(): GameState = currentState
    
    private fun rebuildTiles() {
        val newTiles = mutableListOf<Tile>()
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val value = grid[row][col]
                val id = idGrid[row][col]
                if (value != null && id != null) {
                    newTiles.add(Tile(value = value, id = id, row = row, column = col, merged = false))
                }
            }
        }
        tiles = newTiles
        Log.d(TAG, "rebuildTiles: ${tiles.size} tiles on board: $tiles")
    }
}

// Вспомогательный класс для возвращения нескольких значений
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) 