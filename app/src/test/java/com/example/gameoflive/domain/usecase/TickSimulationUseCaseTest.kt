package com.example.gameoflive.domain.usecase

import com.example.gameoflive.model.Simulation
import org.junit.Test
import org.junit.Assert.*

class TickSimulationUseCaseTest {
    
    @Test
    fun `tick simulation increases tick counter`() {
        // Given
        val simulation = Simulation(width = 10, height = 10)
        val useCase = TickSimulationUseCase()
        val initialTickCount = simulation.tickCounter
        
        // When
        useCase(simulation, 5)
        
        // Then
        assertEquals(initialTickCount + 5, simulation.tickCounter)
    }
    
    @Test
    fun `tick simulation with default count increases by 1`() {
        // Given
        val simulation = Simulation(width = 10, height = 10)
        val useCase = TickSimulationUseCase()
        val initialTickCount = simulation.tickCounter
        
        // When
        useCase(simulation)
        
        // Then
        assertEquals(initialTickCount + 1, simulation.tickCounter)
    }
} 