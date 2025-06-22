package com.example.gameoflive.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameoflive.GameConfig.FAST_FORWARD_TICKS
import com.example.gameoflive.GameConfig.SPAWN_ORGANISM_COUNT
import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.domain.usecase.CalculateMedianGenomeUseCase
import com.example.gameoflive.domain.usecase.SaveSimulationUseCase
import com.example.gameoflive.domain.usecase.SpawnOrganismUseCase
import com.example.gameoflive.domain.usecase.TickSimulationUseCase
import com.example.gameoflive.model.Simulation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: SimulationRepository,
    private val saveSimulationUseCase: SaveSimulationUseCase,
    private val tickSimulationUseCase: TickSimulationUseCase,
    private val spawnOrganismUseCase: SpawnOrganismUseCase,
    private val calculateMedianGenomeUseCase: CalculateMedianGenomeUseCase
) : ViewModel() {

    private var initialSimulation: Simulation? = null

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private val _effect = Channel<GameEffect>()
    val effect = _effect.receiveAsFlow()

    private val intentChannel = Channel<GameIntent>(Channel.UNLIMITED)

    fun initialize(simulation: Simulation) {
        initialSimulation = simulation
        _state.value = GameState(
            simulation = simulation.copyDeep(),
            medianGenome = calculateMedianGenomeUseCase(simulation.organisms.toList())
        )

        viewModelScope.launch {
            while (true) {
                val intent = withTimeoutOrNull(300L) { intentChannel.receive() }
                if (intent != null) {
                    handleIntent(intent)
                } else {
                    initialSimulation?.let { sim ->
                        tickSimulationUseCase(sim)
                        _state.value = _state.value.copy(
                            simulation = sim.copyDeep(),
                            medianGenome = calculateMedianGenomeUseCase(sim.organisms.toList())
                        )
                    }
                }
            }
        }
    }

    fun dispatch(intent: GameIntent) {
        viewModelScope.launch { intentChannel.send(intent) }
    }

    private suspend fun handleIntent(intent: GameIntent) {
        val sim = initialSimulation ?: return

        when (intent) {
            is GameIntent.Init -> {}
            is GameIntent.LoadSimulation -> {
                loadSimulation(intent.fileName)
            }

            is GameIntent.FastForward -> {
                tickSimulationUseCase(sim, FAST_FORWARD_TICKS)
                _state.value = _state.value.copy(
                    simulation = sim.copyDeep(),
                    medianGenome = calculateMedianGenomeUseCase(sim.organisms.toList())
                )
            }

            is GameIntent.CellClicked -> {
                val organism = sim.organisms.firstOrNull {
                    it.position == intent.position
                }
                _state.value = _state.value.copy(selectedOrganism = organism)
            }

            is GameIntent.Save -> {
                if (intent.name.isNotBlank()) {
                    _state.value = _state.value.copy(isSaving = true)
                    saveSimulationUseCase(intent.name, sim)
                    _state.value = _state.value.copy(isSaving = false, showSaveDialog = false)
                    _effect.send(GameEffect.ShowSnackbar("Сохранено!"))
                } else {
                    _state.value = _state.value.copy(showSaveDialog = true)
                }
            }

            is GameIntent.BackPressed -> onBackPressed()
            is GameIntent.ClearSelectedOrganism -> {
                _state.value = _state.value.copy(selectedOrganism = null)
            }

            is GameIntent.CancelSave -> {
                _state.value = _state.value.copy(showSaveDialog = false)
            }

            is GameIntent.SpawnOrganisms -> {
                spawnOrganismUseCase(sim, SPAWN_ORGANISM_COUNT)
                _state.value = _state.value.copy(
                    simulation = sim.copyDeep(),
                    medianGenome = calculateMedianGenomeUseCase(sim.organisms.toList())
                )
            }
        }
    }

    private fun loadSimulation(fileName: String) {
        viewModelScope.launch {
            val simulation = repository.loadSimulation(fileName)
            if (simulation != null) {
                initialize(simulation)
            } else {
                _effect.send(GameEffect.ShowSnackbar("Ошибка загрузки симуляции"))
            }
        }
    }

    fun onBackPressed() {
        _state.value = _state.value.copy(showExitDialog = true)
    }

    fun confirmExit() {
        _state.value = _state.value.copy(showExitDialog = false)
        viewModelScope.launch { _effect.send(GameEffect.NavigateBack) }
    }

    fun cancelExit() {
        _state.value = _state.value.copy(showExitDialog = false)
    }
} 
