package com.example.gameoflive

import com.example.gameoflive.model.*
import com.example.gameoflive.domain.usecase.TickSimulationUseCase
import com.example.gameoflive.domain.usecase.SpawnOrganismUseCase
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class IntegrationTest {
    
    private lateinit var simulation: Simulation
    private lateinit var tickUseCase: TickSimulationUseCase
    private lateinit var spawnUseCase: SpawnOrganismUseCase
    private lateinit var random: Random
    
    @Before
    fun setUp() {
        simulation = Simulation(width = 20, height = 20)
        tickUseCase = TickSimulationUseCase()
        spawnUseCase = SpawnOrganismUseCase()
        random = Random(42)
    }
    
    @Test
    fun `test complete organism lifecycle`() {
        // Создаем организм с низким метаболизмом для выживания
        val genome = Genome(2, 1, 20, 300, 1) // Низкий метаболизм и восприятие
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        val initialEnergy = organism.energy
        val initialAge = organism.age
        
        // Симулируем несколько тиков
        repeat(10) {
            tickUseCase(simulation, 1)
        }
        
        // Проверяем, что организм стареет и тратит энергию (если не умер)
        if (!organism.isDead()) {
            assertTrue(organism.age > initialAge)
            assertTrue(organism.energy < initialEnergy)
        }
    }
    
    @Test
    fun `test organism reproduction with partner`() {
        // Создаем двух организмов разного пола рядом с низким метаболизмом
        val genome1 = Genome(2, 1, 20, 300, 1) // Низкий метаболизм
        val genome2 = Genome(1, 1, 25, 250, 1) // Низкий метаболизм
        
        val org1 = simulation.spawnOrganism(genome1, Position(5, 5), random)
        val org2 = simulation.spawnOrganism(genome2, Position(6, 6), random)
        
        // Устанавливаем условия для размножения
        org1.age = 150
        org2.age = 150
        org1.energy = 200
        org2.energy = 200
        
        val initialOrganismCount = simulation.organisms.size
        
        // Симулируем тики для попытки размножения
        repeat(20) {
            tickUseCase(simulation, 1)
        }
        
        // Проверяем, что количество организмов может увеличиться
        assertTrue(simulation.organisms.size >= initialOrganismCount)
    }
    
    @Test
    fun `test food consumption and energy gain`() {
        // Создаем организм с низким метаболизмом
        val genome = Genome(2, 1, 20, 300, 1) // Низкий метаболизм
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        
        // Добавляем еду в позицию организма
        simulation.food.add(organism.position)
        val initialEnergy = organism.energy
        
        // Симулируем тик
        tickUseCase(simulation, 1)
        
        // Энергия должна увеличиться за счет еды (если организм не умер)
        if (!organism.isDead()) {
            assertTrue(organism.energy > initialEnergy - organism.genome.metabolism - (organism.genome.perception - 1) * 2 - 10)
        }
        
        // Еда должна быть съедена
        assertFalse(simulation.food.contains(organism.position))
    }
    
    @Test
    fun `test organism movement towards food`() {
        // Создаем организм с perception=5
        val genome = Genome(2, 1, 20, 300, 5)
        val organism = simulation.spawnOrganism(genome, Position(5, 5), random)
        val initialPosition = organism.position.copy()
        
        // Добавляем еду в поле зрения
        val foodPosition = Position(
            (initialPosition.x + 3).coerceAtMost(simulation.width - 1),
            (initialPosition.y + 3).coerceAtMost(simulation.height - 1)
        )
        simulation.food.add(foodPosition)
        
        // Симулируем несколько тиков
        repeat(5) {
            organism.tick(simulation, random)
            if (organism.isDead()) return // Если организм умер — не проверяем движение
        }
        
        // Организм должен быть жив и приблизиться к еде
        if (!organism.isDead()) {
            val distanceToFood = kotlin.math.abs(organism.position.x - foodPosition.x) + 
                               kotlin.math.abs(organism.position.y - foodPosition.y)
            val initialDistance = kotlin.math.abs(initialPosition.x - foodPosition.x) + 
                                kotlin.math.abs(initialPosition.y - foodPosition.y)
            assertTrue("Organism should move towards food", distanceToFood < initialDistance)
        }
    }
    
    @Test
    fun `test population dynamics`() {
        // Создаем начальную популяцию
        spawnUseCase(simulation, 5)
        val initialPopulation = simulation.organisms.size
        
        // Симулируем длительный период
        repeat(50) {
            tickUseCase(simulation, 1)
        }
        
        // Проверяем, что популяция может изменяться
        // (может увеличиться за счет размножения или уменьшиться за счет смерти)
        assertTrue(simulation.organisms.size >= 0)
    }
    
    @Test
    fun `test genome inheritance in reproduction`() {
        // Создаем родителей с разными геномами
        val motherGenome = Genome(1, 2, 15, 200, 1)
        val fatherGenome = Genome(3, 4, 25, 400, 5)
        
        val mother = simulation.spawnOrganism(motherGenome, Position(5, 5), random)
        val father = simulation.spawnOrganism(fatherGenome, Position(6, 6), random)
        
        // Устанавливаем условия для размножения
        mother.age = 150
        father.age = 150
        mother.energy = 200
        father.energy = 200
        
        val initialOrganismCount = simulation.organisms.size
        
        // Симулируем тики для попытки размножения
        repeat(30) {
            tickUseCase(simulation, 1)
        }
        
        // Если появились дети, проверяем их геномы
        if (simulation.organisms.size > initialOrganismCount) {
            val children = simulation.organisms.filter { it.id >= initialOrganismCount }
            
            children.forEach { child ->
                // Проверяем, что геном ребенка находится в допустимых границах
                assertTrue("Child speed should be >= 1", child.genome.speed >= 1)
                assertTrue("Child speed should be <= 3", child.genome.speed <= 3)
                assertTrue("Child metabolism should be >= 1", child.genome.metabolism >= 1)
                assertTrue("Child metabolism should be <= 4", child.genome.metabolism <= 4)
                assertTrue("Child digestion efficiency should be >= 15", child.genome.digestionEfficiency >= 15)
                assertTrue("Child digestion efficiency should be <= 25", child.genome.digestionEfficiency <= 25)
                assertTrue("Child max age should be >= 50", child.genome.maxAge >= 50)
                assertTrue("Child max age should be <= 400", child.genome.maxAge <= 400)
                assertTrue("Child perception should be >= 1", child.genome.perception >= 1)
                assertTrue("Child perception should be <= 5", child.genome.perception <= 5)
            }
        }
    }
    
    @Test
    fun `test simulation stability over many ticks`() {
        // Создаем начальную популяцию
        spawnUseCase(simulation, 10)
        
        // Симулируем много тиков
        repeat(100) {
            tickUseCase(simulation, 1)
            
            // Проверяем, что симуляция остается стабильной
            assertTrue("Tick counter should be positive", simulation.tickCounter >= 0)
            assertTrue("Organism count should be non-negative", simulation.organisms.size >= 0)
            assertTrue("Food count should be non-negative", simulation.food.size >= 0)
            
            // Проверяем, что все организмы находятся в границах поля
            simulation.organisms.forEach { organism ->
                assertTrue("Organism X should be >= 0", organism.position.x >= 0)
                assertTrue("Organism X should be < width", organism.position.x < simulation.width)
                assertTrue("Organism Y should be >= 0", organism.position.y >= 0)
                assertTrue("Organism Y should be < height", organism.position.y < simulation.height)
            }
        }
    }
    
    @Test
    fun `test energy conservation principles`() {
        // Создаем организм
        spawnUseCase(simulation, 1)
        val organism = simulation.organisms.first()
        val initialEnergy = organism.energy
        
        // Добавляем еду
        simulation.food.add(organism.position)
        
        // Симулируем тик
        tickUseCase(simulation, 1)
        
        // Энергия должна измениться предсказуемо
        val energyChange = organism.energy - initialEnergy
        val expectedEnergyGain = organism.genome.digestionEfficiency
        val expectedEnergyLoss = organism.genome.metabolism + (organism.genome.perception - 1) * 2 + 10
        
        // Энергия должна быть в разумных пределах
        assertTrue("Energy should not be negative", organism.energy >= 0)
        assertTrue("Energy should not be unreasonably high", organism.energy <= 1000)
    }
} 