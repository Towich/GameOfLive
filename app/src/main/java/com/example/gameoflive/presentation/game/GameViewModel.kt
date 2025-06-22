package com.example.gameoflive.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameoflive.GameConfig.FAST_FORWARD_TICKS
import com.example.gameoflive.GameConfig.SPAWN_ORGANISM_COUNT
import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.domain.usecase.SaveSimulationUseCase
import com.example.gameoflive.domain.usecase.SpawnOrganismUseCase
import com.example.gameoflive.domain.usecase.TickSimulationUseCase
import com.example.gameoflive.model.Simulation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class GameViewModel(
    private val repository: SimulationRepository,
    private val initialSimulation: Simulation
) : ViewModel() {

    private val saveSimulationUseCase = SaveSimulationUseCase(repository)
    private val tickSimulationUseCase = TickSimulationUseCase()
    private val spawnOrganismUseCase = SpawnOrganismUseCase()

    private val _state = MutableStateFlow(GameState(simulation = initialSimulation.copyDeep()))
    val state: StateFlow<GameState> = _state

    private val _effect = Channel<GameEffect>()
    val effect = _effect.receiveAsFlow()

    private val intentChannel = Channel<GameIntent>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            while (true) {
                val intent = withTimeoutOrNull(300L) { intentChannel.receive() }
                if (intent != null) {
                    handleIntent(intent)
                } else {
                    tickSimulationUseCase(initialSimulation)
                    _state.value = _state.value.copy(simulation = initialSimulation.copyDeep())
                }
            }
        }
    }

    fun dispatch(intent: GameIntent) {
        viewModelScope.launch { intentChannel.send(intent) }
    }

    private suspend fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.Init -> {}
            is GameIntent.FastForward -> {
                tickSimulationUseCase(initialSimulation, FAST_FORWARD_TICKS)
                _state.value = _state.value.copy(simulation = initialSimulation.copyDeep())
            }

            is GameIntent.CellClicked -> {
                val organism = initialSimulation.organisms.firstOrNull {
                    it.position == intent.position
                }
                _state.value = _state.value.copy(selectedOrganism = organism)
            }

            is GameIntent.Save -> {
                if (intent.name.isNotBlank()) {
                    _state.value = _state.value.copy(isSaving = true)
                    saveSimulationUseCase(intent.name, initialSimulation)
                    _state.value = _state.value.copy(isSaving = false, showSaveDialog = false)
                    _effect.send(GameEffect.ShowSnackbar("Сохранено!"))
                } else {
                    _state.value = _state.value.copy(showSaveDialog = true)
                }
            }

            is GameIntent.BackPressed -> _effect.send(GameEffect.NavigateBack)
            is GameIntent.ClearSelectedOrganism -> {
                _state.value = _state.value.copy(selectedOrganism = null)
            }

            is GameIntent.CancelSave -> {
                _state.value = _state.value.copy(showSaveDialog = false)
            }

            is GameIntent.SpawnOrganisms -> {
                spawnOrganismUseCase(initialSimulation, SPAWN_ORGANISM_COUNT)
                _state.value = _state.value.copy(simulation = initialSimulation.copyDeep())
            }
        }
    }
} 