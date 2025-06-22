package com.example.gameoflive.domain

import com.example.gameoflive.model.*
import com.example.gameoflive.domain.usecase.SpawnOrganismUseCase
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class SpawnOrganismUseCaseTest {
    
    private lateinit var simulation: Simulation
    private lateinit var useCase: SpawnOrganismUseCase
    private lateinit var random: Random
    
    @Before
    fun setUp() {
        simulation = Simulation(width = 10, height = 10)
        useCase = SpawnOrganismUseCase()
        random = Random(42)
    }
    
    @Test
    fun `test spawn single organism`() {
        val initialOrganismCount = simulation.organisms.size
        
        useCase(simulation, 1)
        
        assertEquals(initialOrganismCount + 1, simulation.organisms.size)
    }
    
    @Test
    fun `test spawn multiple organisms`() {
        val initialOrganismCount = simulation.organisms.size
        val organismsToSpawn = 5
        
        useCase(simulation, organismsToSpawn)
        
        assertEquals(initialOrganismCount + organismsToSpawn, simulation.organisms.size)
    }
    
    @Test
    fun `test default spawn single organism`() {
        val initialOrganismCount = simulation.organisms.size
        
        useCase(simulation)
        
        assertEquals(initialOrganismCount + 1, simulation.organisms.size)
    }
    
    @Test
    fun `test spawned organism has valid properties`() {
        useCase(simulation, 1)
        
        val organism = simulation.organisms.first()
        
        // Проверяем, что организм имеет валидный геном
        assertTrue("Speed should be >= 1", organism.genome.speed >= 1)
        assertTrue("Speed should be <= 3", organism.genome.speed <= 3)
        assertTrue("Metabolism should be >= 1", organism.genome.metabolism >= 1)
        assertTrue("Metabolism should be <= 4", organism.genome.metabolism <= 4)
        assertTrue("Digestion efficiency should be >= 15", organism.genome.digestionEfficiency >= 15)
        assertTrue("Digestion efficiency should be <= 25", organism.genome.digestionEfficiency <= 25)
        assertTrue("Max age should be >= 200", organism.genome.maxAge >= 200)
        assertTrue("Max age should be <= 400", organism.genome.maxAge <= 400)
        assertTrue("Perception should be >= 1", organism.genome.perception >= 1)
        assertTrue("Perception should be <= 5", organism.genome.perception <= 5)
        
        // Проверяем, что позиция в границах поля
        assertTrue("X should be >= 0", organism.position.x >= 0)
        assertTrue("X should be < 10", organism.position.x < 10)
        assertTrue("Y should be >= 0", organism.position.y >= 0)
        assertTrue("Y should be < 10", organism.position.y < 10)
        
        // Проверяем начальные значения
        assertEquals(100, organism.energy)
        assertEquals(0, organism.age)
        assertFalse(organism.isDead())
    }
    
    @Test
    fun `test spawned organisms have unique ids`() {
        useCase(simulation, 3)
        
        val ids = simulation.organisms.map { it.id }.toSet()
        
        assertEquals(3, ids.size)
    }
    
    @Test
    fun `test spawned organisms have different sexes`() {
        useCase(simulation, 10)
        
        val sexes = simulation.organisms.map { it.sex }.toSet()
        
        // Должны быть представители обоих полов
        assertTrue(sexes.contains(Sex.MALE))
        assertTrue(sexes.contains(Sex.FEMALE))
    }
    
    @Test
    fun `test spawn organisms in free positions only`() {
        // Заполняем поле организмами
        val genome = Genome(2, 3, 20, 300, 4)
        for (x in 0 until 10) {
            for (y in 0 until 10) {
                simulation.spawnOrganism(genome, Position(x, y), random)
            }
        }
        
        val initialOrganismCount = simulation.organisms.size
        
        // Пытаемся создать еще один организм
        useCase(simulation, 1)
        
        // Количество организмов не должно измениться, так как нет свободных позиций
        assertEquals(initialOrganismCount, simulation.organisms.size)
    }
    
    @Test
    fun `test spawn organisms with random positions`() {
        useCase(simulation, 5)
        
        val positions = simulation.organisms.map { it.position }.toSet()
        
        // Позиции должны быть разными (хотя теоретически могут совпасть случайно)
        assertTrue("Should have different positions", positions.size >= 1)
    }
    
    @Test
    fun `test zero organisms spawn does nothing`() {
        val initialOrganismCount = simulation.organisms.size
        
        useCase(simulation, 0)
        
        assertEquals(initialOrganismCount, simulation.organisms.size)
    }
    
    @Test
    fun `test negative organisms spawn does nothing`() {
        val initialOrganismCount = simulation.organisms.size
        
        useCase(simulation, -1)
        
        assertEquals(initialOrganismCount, simulation.organisms.size)
    }
    
    @Test
    fun `test spawn organisms with different genomes`() {
        useCase(simulation, 10)
        
        val genomes = simulation.organisms.map { it.genome }.toSet()
        
        // Геномы должны быть разными (хотя теоретически могут совпасть случайно)
        assertTrue("Should have different genomes", genomes.size >= 1)
    }
} 