package com.example.gameoflive.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.model.Simulation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameLoaderViewModel @Inject constructor(
    private val repository: SimulationRepository
) : ViewModel() {
    private val _simulation = MutableStateFlow<Simulation?>(null)
    val simulation: StateFlow<Simulation?> = _simulation

    fun loadSimulation(fileName: String) {
        viewModelScope.launch {
            _simulation.value = repository.loadSimulation(fileName)
        }
    }
} 