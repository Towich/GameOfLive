package com.example.gameoflive

import com.example.gameoflive.model.*
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class SimulationTest {
    
    private lateinit var simulation: Simulation
    private lateinit var random: Random
    
    @Before
    fun setUp() {
        simulation = Simulation(width = 10, height = 10)
        random = Random(42)
    }
    
    @Test
    fun `test simulation creation`() {
        assertEquals(10, simulation.width)
        assertEquals(10, simulation.height)
        assertEquals(0, simulation.tickCounter)
        assertEquals(0, simulation.organisms.size)
        assertEquals(0, simulation.food.size)
    }
    
    @Test
    fun `test spawn organism`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val position = Position(5, 5)
        
        val organism = simulation.spawnOrganism(genome, position, random)
        
        assertEquals(0, organism.id)
        assertEquals(position, organism.position)
        assertEquals(genome, organism.genome)
        assertEquals(1, simulation.organisms.size)
        assertTrue(simulation.organisms.contains(organism))
    }
    
    @Test
    fun `test organism id increment`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val position = Position(5, 5)
        
        val org1 = simulation.spawnOrganism(genome, position, random)
        val org2 = simulation.spawnOrganism(genome, position, random)
        
        assertEquals(0, org1.id)
        assertEquals(1, org2.id)
    }
    
    @Test
    fun `test consume food at position`() {
        val position = Position(5, 5)
        simulation.food.add(position)
        
        assertTrue(simulation.consumeFoodAt(position))
        assertFalse(simulation.food.contains(position))
    }
    
    @Test
    fun `test consume food at empty position`() {
        val position = Position(5, 5)
        
        assertFalse(simulation.consumeFoodAt(position))
    }
    
    @Test
    fun `test is cell free`() {
        val position = Position(5, 5)
        
        assertTrue(simulation.isCellFree(position))
        
        val genome = Genome(2, 3, 20, 300, 4)
        simulation.spawnOrganism(genome, position, random)
        
        assertFalse(simulation.isCellFree(position))
    }
    
    @Test
    fun `test clamp position within bounds`() {
        val position = Position(5, 5)
        val clamped = simulation.clampPosition(position)
        
        assertEquals(position, clamped)
    }
    
    @Test
    fun `test clamp position outside bounds`() {
        val position = Position(-1, 15)
        val clamped = simulation.clampPosition(position)
        
        assertEquals(0, clamped.x)
        assertEquals(9, clamped.y)
    }
    
    @Test
    fun `test find partner nearby`() {
        val genome1 = Genome(2, 3, 20, 300, 4)
        val genome2 = Genome(1, 4, 25, 250, 5)
        
        val org1 = simulation.spawnOrganism(genome1, Position(5, 5), random)
        val org2 = simulation.spawnOrganism(genome2, Position(6, 6), random)
        
        val partner = simulation.findPartnerNearby(org1, 2)
        
        assertEquals(org2, partner)
    }
    
    @Test
    fun `test find partner nearby out of range`() {
        val genome1 = Genome(2, 3, 20, 300, 4)
        val genome2 = Genome(1, 4, 25, 250, 5)
        
        val org1 = simulation.spawnOrganism(genome1, Position(5, 5), random)
        val org2 = simulation.spawnOrganism(genome2, Position(8, 8), random)
        
        val partner = simulation.findPartnerNearby(org1, 2)
        
        assertNull(partner)
    }
    
    @Test
    fun `test find partner nearby excludes self`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val org = simulation.spawnOrganism(genome, Position(5, 5), random)
        
        val partner = simulation.findPartnerNearby(org, 5)
        
        assertNull(partner)
    }
    
    @Test
    fun `test find free adjacent position`() {
        val position = Position(5, 5)
        
        val freePos = simulation.findFreeAdjacent(position)
        
        assertNotNull(freePos)
        assertTrue(simulation.isCellFree(freePos!!))
        
        // Позиция должна быть смежной
        val dx = kotlin.math.abs(freePos.x - position.x)
        val dy = kotlin.math.abs(freePos.y - position.y)
        assertTrue("Should be adjacent", dx <= 1 && dy <= 1)
    }
    
    @Test
    fun `test find free adjacent position when surrounded`() {
        val position = Position(5, 5)
        val genome = Genome(2, 3, 20, 300, 4)
        
        // Заполняем все соседние позиции
        val adjacentPositions = listOf(
            Position(4, 4), Position(4, 5), Position(4, 6),
            Position(5, 4), Position(5, 6),
            Position(6, 4), Position(6, 5), Position(6, 6)
        )
        
        adjacentPositions.forEach { pos ->
            simulation.spawnOrganism(genome, pos, random)
        }
        
        val freePos = simulation.findFreeAdjacent(position)
        
        assertNull(freePos)
    }
    
    @Test
    fun `test find nearest food`() {
        val fromPosition = Position(5, 5)
        simulation.food.add(Position(7, 7)) // Дальняя еда
        simulation.food.add(Position(6, 6)) // Ближняя еда
        simulation.food.add(Position(8, 8)) // Самая дальняя еда
        
        val nearestFood = simulation.findNearestFood(fromPosition, 5)
        
        assertEquals(Position(6, 6), nearestFood)
    }
    
    @Test
    fun `test find nearest food out of range`() {
        val fromPosition = Position(5, 5)
        simulation.food.add(Position(8, 8)) // Еда вне радиуса восприятия
        
        val nearestFood = simulation.findNearestFood(fromPosition, 2)
        
        assertNull(nearestFood)
    }
    
    @Test
    fun `test find nearest food when no food`() {
        val fromPosition = Position(5, 5)
        
        val nearestFood = simulation.findNearestFood(fromPosition, 5)
        
        assertNull(nearestFood)
    }
    
    @Test
    fun `test simulation tick counter increment`() {
        val initialTick = simulation.tickCounter
        
        simulation.tick()
        
        assertEquals(initialTick + 1, simulation.tickCounter)
    }
    
    @Test
    fun `test simulation tick spawns food`() {
        val initialFoodCount = simulation.food.size
        
        simulation.tick()
        
        // Еда может появиться (не гарантированно из-за случайности)
        assertTrue(simulation.food.size >= initialFoodCount)
    }
    
    @Test
    fun `test simulation tick processes organisms`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        val initialAge = organism.age
        
        simulation.tick()
        
        assertEquals(initialAge + 1, organism.age)
    }
    
    @Test
    fun `test simulation tick removes dead organisms`() {
        val genome = Genome(2, 100, 20, 300, 4) // Высокий метаболизм для быстрой смерти
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        
        simulation.tick()
        
        assertFalse(simulation.organisms.contains(organism))
        assertEquals(0, simulation.organisms.size)
    }
    
    @Test
    fun `test simulation deep copy`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        simulation.food.add(Position(3, 3))
        simulation.tick()
        
        val copiedSimulation = simulation.copyDeep()
        
        assertEquals(simulation.width, copiedSimulation.width)
        assertEquals(simulation.height, copiedSimulation.height)
        assertEquals(simulation.tickCounter, copiedSimulation.tickCounter)
        assertEquals(simulation.nextId, copiedSimulation.nextId)
        assertEquals(simulation.food.size, copiedSimulation.food.size)
        assertEquals(simulation.organisms.size, copiedSimulation.organisms.size)
        
        // Проверяем, что это действительно глубокое копирование
        assertNotSame(simulation.food, copiedSimulation.food)
        assertNotSame(simulation.organisms, copiedSimulation.organisms)
        
        // Проверяем, что организмы скопированы правильно
        val originalOrg = simulation.organisms.first()
        val copiedOrg = copiedSimulation.organisms.first()
        
        assertEquals(originalOrg.id, copiedOrg.id)
        assertEquals(originalOrg.position, copiedOrg.position)
        assertEquals(originalOrg.genome, copiedOrg.genome)
        assertNotSame(originalOrg.position, copiedOrg.position)
    }
    
    @Test
    fun `test organism copy`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val original = Organism(1, Position(5, 5), genome, Sex.MALE, 100, 10)
        
        val copied = original.copyOrganism()
        
        assertEquals(original.id, copied.id)
        assertEquals(original.position, copied.position)
        assertEquals(original.genome, copied.genome)
        assertEquals(original.sex, copied.sex)
        assertEquals(original.energy, copied.energy)
        assertEquals(original.age, copied.age)
        
        // Проверяем, что это действительно копия
        assertNotSame(original.position, copied.position)
    }
} 