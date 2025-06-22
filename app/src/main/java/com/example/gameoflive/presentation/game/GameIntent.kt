package com.example.gameoflive.presentation.game

import com.example.gameoflive.model.Position

sealed interface GameIntent {
    data object Init : GameIntent
    data object FastForward : GameIntent
    data class CellClicked(val position: Position) : GameIntent
    data class Save(val name: String) : GameIntent
    data object BackPressed : GameIntent
} 