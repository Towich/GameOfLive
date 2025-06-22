package com.example.gameoflive.presentation.game

import com.example.gameoflive.model.Organism
import com.example.gameoflive.model.Simulation

// Первоначальное состояние создаётся с пустой симуляцией.
data class GameState(
    val simulation: Simulation? = null,
    val selectedOrganism: Organism? = null,
    val isSaving: Boolean = false,
    val showSaveDialog: Boolean = false,
    val showExitDialog: Boolean = false,
    val error: String? = null
) 