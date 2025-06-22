package com.example.gameoflive.domain.usecase

import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.model.Simulation

class SaveSimulationUseCase(
    private val repository: SimulationRepository
) {
    suspend operator fun invoke(name: String, simulation: Simulation) {
        repository.saveSimulation(name, simulation)
    }
} 