package com.example.gameoflive.domain.usecase

import com.example.gameoflive.model.Simulation

class TickSimulationUseCase {
    operator fun invoke(simulation: Simulation, ticks: Int = 1) {
        repeat(ticks) {
            simulation.tick()
        }
    }
} 