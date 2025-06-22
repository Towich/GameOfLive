package com.example.gameoflive.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.model.Simulation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: SimulationRepository,
    private val initialSimulation: Simulation
) : ViewModel() {

    private val _state = MutableStateFlow(GameState(simulation = initialSimulation))
    val state: StateFlow<GameState> = _state

    private val _effect = Channel<GameEffect>()
    val effect = _effect.receiveAsFlow()

    private val intentFlow = MutableSharedFlow<GameIntent>()

    init {
        // стартуем подписку на симуляцию
        viewModelScope.launch {
            repository.observeSimulation(initialSimulation).collect { sim ->
                _state.value = _state.value.copy(simulation = sim)
            }
        }
        // обработка интентов
        viewModelScope.launch {
            intentFlow.collect { intent -> handleIntent(intent) }
        }
    }

    fun dispatch(intent: GameIntent) {
        viewModelScope.launch { intentFlow.emit(intent) }
    }

    private suspend fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.FastForward -> {
                repeat(10) { initialSimulation.tick() }
            }
            is GameIntent.CellClicked -> {
                val organism = initialSimulation.organisms.firstOrNull {
                    it.position == intent.position
                }
                _state.value = _state.value.copy(selectedOrganism = organism)
            }
            is GameIntent.Save -> {
                _state.value = _state.value.copy(isSaving = true)
                repository.saveSimulation(intent.name, initialSimulation)
                _state.value = _state.value.copy(isSaving = false)
                _effect.send(GameEffect.ShowSnackbar("Сохранено!"))
            }
            is GameIntent.BackPressed -> _effect.send(GameEffect.NavigateBack)
            else -> Unit
        }
    }
} 