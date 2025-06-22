package com.example.gameoflive.domain.usecase

import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.model.Simulation

class LoadSimulationUseCase(
    private val repository: SimulationRepository
) {
    suspend operator fun invoke(fileName: String): Simulation? {
        return repository.loadSimulation(fileName)
    }
} 