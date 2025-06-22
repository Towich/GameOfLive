package com.example.gameoflive.domain

import com.example.gameoflive.model.*
import com.example.gameoflive.domain.usecase.TickSimulationUseCase
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class TickSimulationUseCaseTest {
    
    private lateinit var simulation: Simulation
    private lateinit var useCase: TickSimulationUseCase
    private lateinit var random: Random
    
    @Before
    fun setUp() {
        simulation = Simulation(width = 10, height = 10)
        useCase = TickSimulationUseCase()
        random = Random(42)
    }
    
    @Test
    fun `test single tick execution`() {
        val initialTickCounter = simulation.tickCounter
        
        useCase(simulation, 1)
        
        assertEquals(initialTickCounter + 1, simulation.tickCounter)
    }
    
    @Test
    fun `test multiple ticks execution`() {
        val initialTickCounter = simulation.tickCounter
        val ticksToExecute = 5
        
        useCase(simulation, ticksToExecute)
        
        assertEquals(initialTickCounter + ticksToExecute, simulation.tickCounter)
    }
    
    @Test
    fun `test default single tick`() {
        val initialTickCounter = simulation.tickCounter
        
        useCase(simulation)
        
        assertEquals(initialTickCounter + 1, simulation.tickCounter)
    }
    
    @Test
    fun `test tick processes organisms`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        val initialAge = organism.age
        
        useCase(simulation, 1)
        
        assertEquals(initialAge + 1, organism.age)
    }
    
    @Test
    fun `test tick spawns food`() {
        val initialFoodCount = simulation.food.size
        
        useCase(simulation, 1)
        
        // Еда может появиться (не гарантированно из-за случайности)
        assertTrue(simulation.food.size >= initialFoodCount)
    }
    
    @Test
    fun `test tick removes dead organisms`() {
        val genome = Genome(2, 100, 20, 300, 4) // Высокий метаболизм для быстрой смерти
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        
        useCase(simulation, 1)
        
        assertFalse(simulation.organisms.contains(organism))
        assertEquals(0, simulation.organisms.size)
    }
    
    @Test
    fun `test multiple ticks with organism lifecycle`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        val initialAge = organism.age
        val initialEnergy = organism.energy
        
        useCase(simulation, 3)
        
        assertEquals(initialAge + 3, organism.age)
        // Энергия должна уменьшиться на каждый тик
        assertTrue(organism.energy < initialEnergy)
    }
    
    @Test
    fun `test zero ticks does nothing`() {
        val initialTickCounter = simulation.tickCounter
        
        useCase(simulation, 0)
        
        assertEquals(initialTickCounter, simulation.tickCounter)
    }
    
    @Test
    fun `test negative ticks does nothing`() {
        val initialTickCounter = simulation.tickCounter
        
        useCase(simulation, -1)
        
        assertEquals(initialTickCounter, simulation.tickCounter)
    }
} 