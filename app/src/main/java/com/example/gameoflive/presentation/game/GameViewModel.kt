package com.example.gameoflive.presentation.game

import android.util.Log
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GameEffect>()
    val effect = _effect.asSharedFlow()

    private val _intent = MutableSharedFlow<GameIntent>()

    init {
        startGameLoop()
    }

    fun initialize(simulation: Simulation) {
        Log.d("GameViewModel", "Инициализация с симуляцией")
        initialSimulation = simulation
        _state.value = GameState(
            simulation = simulation.copyDeep(),
            medianGenome = calculateMedianGenomeUseCase(simulation.organisms.toList())
        )
    }

    fun dispatch(intent: GameIntent) {
        Log.d("GameViewModel", "Получен intent: $intent")
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            Log.d("GameViewModel", "Запуск игрового цикла")
            
            // Создаем Flow для автоматического тика симуляции
            val tickFlow = flow {
                while (true) {
                    delay(300) // 300ms между тиками
                    emit(Unit)
                }
            }
            
            // Объединяем интенты и автоматические тики
            merge(_intent, tickFlow).collect { event ->
                when (event) {
                    is GameIntent -> {
                        Log.d("GameViewModel", "Обрабатываем intent: $event")
                        handleIntent(event)
                    }
                    Unit -> {
                        // Автоматический тик симуляции
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
    }

    private suspend fun handleIntent(intent: GameIntent) {
        val sim = initialSimulation ?: return

        when (intent) {
            is GameIntent.Init -> {}

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
                    _effect.emit(GameEffect.ShowSnackbar("Сохранено!"))
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

    fun loadSimulation(fileName: String) {
        viewModelScope.launch {
            Log.d("GameViewModel", "Начало загрузки симуляции: $fileName")
            val simulation = repository.loadSimulation(fileName)
            if (simulation != null) {
                Log.d("GameViewModel", "Симуляция успешно загружена: $fileName")
                initialSimulation = simulation
                _state.value = _state.value.copy(
                    simulation = simulation.copyDeep(),
                    medianGenome = calculateMedianGenomeUseCase(simulation.organisms.toList())
                )
            } else {
                Log.e("GameViewModel", "Ошибка загрузки симуляции: $fileName")
                _effect.emit(GameEffect.ShowSnackbar("Ошибка загрузки симуляции"))
            }
        }
    }

    fun onBackPressed() {
        _state.value = _state.value.copy(showExitDialog = true)
    }

    fun confirmExit() {
        _state.value = _state.value.copy(showExitDialog = false)
        viewModelScope.launch { _effect.emit(GameEffect.NavigateBack) }
    }

    fun cancelExit() {
        _state.value = _state.value.copy(showExitDialog = false)
    }
} 
