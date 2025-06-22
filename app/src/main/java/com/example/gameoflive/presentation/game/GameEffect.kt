package com.example.gameoflive.presentation.game

sealed interface GameEffect {
    data class ShowSnackbar(val message: String) : GameEffect
    data object NavigateBack : GameEffect
} 