package com.example.a2048.data.model

import androidx.compose.ui.graphics.Color
import com.example.a2048.ui.theme.Tile1024
import com.example.a2048.ui.theme.Tile128
import com.example.a2048.ui.theme.Tile16
import com.example.a2048.ui.theme.Tile2
import com.example.a2048.ui.theme.Tile2048
import com.example.a2048.ui.theme.Tile256
import com.example.a2048.ui.theme.Tile32
import com.example.a2048.ui.theme.Tile4
import com.example.a2048.ui.theme.Tile512
import com.example.a2048.ui.theme.Tile64
import com.example.a2048.ui.theme.Tile8
import com.example.a2048.ui.theme.TileSuper

/**
 * Represents a tile in the 2048 game.
 * @param value The numerical value of the tile (2, 4, 8, etc.)
 * @param id Unique identifier for the tile
 * @param row Current row position in the grid
 * @param column Current column position in the grid
 * @param merged Whether this tile was created by merging two tiles
 */
data class Tile(
    val value: Int = 0,
    val id: Int = 0,
    val row: Int = 0,
    val column: Int = 0,
    val merged: Boolean = false
) {
    // Get the appropriate color for the tile based on its value
    fun getColor(): Color {
        return when (value) {
            2 -> Tile2
            4 -> Tile4
            8 -> Tile8
            16 -> Tile16
            32 -> Tile32
            64 -> Tile64
            128 -> Tile128
            256 -> Tile256
            512 -> Tile512
            1024 -> Tile1024
            2048 -> Tile2048
            else -> TileSuper
        }
    }
    
    // Get the appropriate text color for the tile
    fun getTextColor(): Color {
        return if (value <= 4) Color(0xFF776E65) else Color.White
    }
    
    // Get the font size for the tile value
    fun getFontSize(): Int {
        return when {
            value < 100 -> 36
            value < 1000 -> 32
            else -> 24
        }
    }
} 