package com.example.gameoflive

import com.example.gameoflive.model.*
import com.example.gameoflive.domain.usecase.TickSimulationUseCase
import com.example.gameoflive.domain.usecase.SpawnOrganismUseCase
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class BoundaryTest {
    
    private lateinit var simulation: Simulation
    private lateinit var tickUseCase: TickSimulationUseCase
    private lateinit var spawnUseCase: SpawnOrganismUseCase
    private lateinit var random: Random
    
    @Before
    fun setUp() {
        simulation = Simulation(width = 5, height = 5) // Маленькое поле для граничных случаев
        tickUseCase = TickSimulationUseCase()
        spawnUseCase = SpawnOrganismUseCase()
        random = Random(42)
    }
    
    @Test
    fun `test organism at field boundaries`() {
        val genome = Genome(2, 1, 20, 300, 1)
        
        // Создаем организмы в углах поля
        val cornerPositions = listOf(
            Position(0, 0), Position(4, 0), Position(0, 4), Position(4, 4)
        )
        
        cornerPositions.forEach { position ->
            val organism = simulation.spawnOrganism(genome, position, random)
            
            // Симулируем несколько тиков
            repeat(5) {
                organism.tick(simulation, random)
                
                // Организм должен оставаться в границах поля
                assertTrue("X should be >= 0", organism.position.x >= 0)
                assertTrue("X should be < 5", organism.position.x < 5)
                assertTrue("Y should be >= 0", organism.position.y >= 0)
                assertTrue("Y should be < 5", organism.position.y < 5)
            }
        }
    }
    
    @Test
    fun `test organism with maximum genome values`() {
        val maxGenome = Genome(3, 4, 25, 400, 5) // Максимальные значения
        val organism = simulation.spawnOrganism(maxGenome, Position(2, 2), random)
        
        // Симулируем тик
        organism.tick(simulation, random)
        
        // Организм должен выжить хотя бы один тик
        assertTrue(organism.age > 0)
    }
    
    @Test
    fun `test organism with minimum genome values`() {
        val minGenome = Genome(1, 1, 15, 200, 1) // Минимальные значения
        val organism = simulation.spawnOrganism(minGenome, Position(2, 2), random)
        
        // Симулируем тик
        organism.tick(simulation, random)
        
        // Организм должен выжить хотя бы один тик
        assertTrue(organism.age > 0)
    }
    
    @Test
    fun `test organism with very high metabolism`() {
        val highMetabolismGenome = Genome(2, 150, 20, 300, 4) // Очень высокий метаболизм
        val organism = simulation.spawnOrganism(highMetabolismGenome, Position(2, 2), random)
        
        // Организм должен умереть за несколько тиков
        var ticks = 0
        while (!organism.isDead() && ticks < 10) {
            organism.tick(simulation, random)
            ticks++
        }
        assertTrue(organism.isDead())
    }
    
    @Test
    fun `test organism with very low max age`() {
        val lowAgeGenome = Genome(2, 1, 20, 1, 1) // Очень короткая жизнь
        val organism = simulation.spawnOrganism(lowAgeGenome, Position(2, 2), random)
        
        // Первый тик — организм еще жив
        organism.tick(simulation, random)
        assertFalse(organism.isDead())
        // Второй тик — должен умереть от старости
        organism.tick(simulation, random)
        assertTrue(organism.isDead())
    }
    
    @Test
    fun `test organism with very high perception cost`() {
        val highPerceptionGenome = Genome(2, 1, 20, 300, 5) // Максимальное восприятие
        val organism = simulation.spawnOrganism(highPerceptionGenome, Position(2, 2), random)
        val initialEnergy = organism.energy
        
        organism.tick(simulation, random)
        
        // Энергия должна значительно уменьшиться из-за высокой стоимости восприятия
        val energyLoss = initialEnergy - organism.energy
        val expectedPerceptionCost = (highPerceptionGenome.perception - 1) * 2
        assertTrue("Energy loss should include perception cost", energyLoss >= expectedPerceptionCost)
    }
    
    @Test
    fun `test simulation with maximum organisms`() {
        val genome = Genome(2, 1, 20, 300, 1)
        
        // Заполняем все поле организмами
        for (x in 0 until 5) {
            for (y in 0 until 5) {
                simulation.spawnOrganism(genome, Position(x, y), random)
            }
        }
        
        assertEquals(25, simulation.organisms.size)
        
        // Пытаемся создать еще один организм
        spawnUseCase(simulation, 1)
        
        // Количество не должно измениться
        assertEquals(25, simulation.organisms.size)
    }
    
    @Test
    fun `test simulation with maximum food`() {
        // Заполняем все поле едой
        for (x in 0 until 5) {
            for (y in 0 until 5) {
                simulation.food.add(Position(x, y))
            }
        }
        
        assertEquals(25, simulation.food.size)
        
        // Симулируем тик
        tickUseCase(simulation, 1)
        
        // Еда может появиться или исчезнуть
        assertTrue(simulation.food.size >= 0)
    }
    
    @Test
    fun `test organism reproduction with maximum energy`() {
        val genome = Genome(2, 1, 20, 300, 1)
        val organism = simulation.spawnOrganism(genome, Position(2, 2), random)
        
        // Устанавливаем максимальную энергию
        organism.energy = 1000
        organism.age = 150
        
        // Симулируем тик
        organism.tick(simulation, random)
        
        // Организм должен остаться живым
        assertFalse(organism.isDead())
    }
    
    @Test
    fun `test organism with zero energy`() {
        val genome = Genome(2, 1, 20, 300, 1)
        val organism = simulation.spawnOrganism(genome, Position(2, 2), random)
        
        // Устанавливаем нулевую энергию
        organism.energy = 0
        
        // Симулируем тик
        organism.tick(simulation, random)
        
        // Организм должен умереть
        assertTrue(organism.isDead())
    }
    
    @Test
    fun `test organism movement with maximum speed`() {
        val maxSpeedGenome = Genome(3, 1, 20, 300, 1) // Максимальная скорость
        val organism = simulation.spawnOrganism(maxSpeedGenome, Position(2, 2), random)
        val initialPosition = organism.position.copy()
        
        organism.tick(simulation, random)
        
        // Организм должен двигаться
        assertNotEquals(initialPosition, organism.position)
    }
    
    @Test
    fun `test organism movement with minimum speed`() {
        val minSpeedGenome = Genome(1, 1, 20, 300, 1) // Минимальная скорость
        val organism = simulation.spawnOrganism(minSpeedGenome, Position(2, 2), random)
        val initialPosition = organism.position.copy()
        
        organism.tick(simulation, random)
        
        // Организм должен двигаться (даже с минимальной скоростью)
        assertNotEquals(initialPosition, organism.position)
    }
    
    @Test
    fun `test simulation deep copy with empty state`() {
        val copiedSimulation = simulation.copyDeep()
        
        assertEquals(simulation.width, copiedSimulation.width)
        assertEquals(simulation.height, copiedSimulation.height)
        assertEquals(simulation.tickCounter, copiedSimulation.tickCounter)
        assertEquals(simulation.organisms.size, copiedSimulation.organisms.size)
        assertEquals(simulation.food.size, copiedSimulation.food.size)
    }
    
    @Test
    fun `test simulation deep copy with full state`() {
        // Заполняем симуляцию
        val genome = Genome(2, 1, 20, 300, 1)
        for (x in 0 until 5) {
            for (y in 0 until 5) {
                simulation.spawnOrganism(genome, Position(x, y), random)
                simulation.food.add(Position(x, y))
            }
        }
        simulation.tick()
        
        val copiedSimulation = simulation.copyDeep()
        
        assertEquals(simulation.width, copiedSimulation.width)
        assertEquals(simulation.height, copiedSimulation.height)
        assertEquals(simulation.tickCounter, copiedSimulation.tickCounter)
        assertEquals(simulation.organisms.size, copiedSimulation.organisms.size)
        assertEquals(simulation.food.size, copiedSimulation.food.size)
        
        // Проверяем, что это действительно копии
        assertNotSame(simulation.organisms, copiedSimulation.organisms)
        assertNotSame(simulation.food, copiedSimulation.food)
    }
} 