package com.example.gameoflive

import com.example.gameoflive.model.*
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class OrganismTest {
    
    private lateinit var simulation: Simulation
    private lateinit var random: Random
    
    @Before
    fun setUp() {
        simulation = Simulation(width = 10, height = 10)
        random = Random(42)
    }
    
    @Test
    fun `test organism creation`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val position = Position(5, 5)
        val organism = Organism(1, position, genome)
        
        assertEquals(1, organism.id)
        assertEquals(position, organism.position)
        assertEquals(genome, organism.genome)
        assertEquals(100, organism.energy)
        assertEquals(0, organism.age)
        assertFalse(organism.isDead())
    }
    
    @Test
    fun `test organism death by old age`() {
        val genome = Genome(2, 3, 20, 5, 4) // Очень короткая жизнь
        val organism = Organism(1, Position(5, 5), genome)
        
        // Симулируем 6 тиков (больше maxAge)
        repeat(6) {
            organism.tick(simulation, random)
        }
        
        assertTrue(organism.isDead())
        assertEquals(Organism.Phase.DEATH, organism.phase)
    }
    
    @Test
    fun `test organism death by energy depletion`() {
        val genome = Genome(2, 50, 20, 300, 4) // Высокий метаболизм
        val organism = Organism(1, Position(5, 5), genome)
        
        // Симулируем тики до истощения энергии
        repeat(3) {
            organism.tick(simulation, random)
        }
        
        assertTrue(organism.isDead())
        assertEquals(Organism.Phase.DEATH, organism.phase)
    }
    
    @Test
    fun `test organism growth phase`() {
        val genome = Genome(2, 1, 20, 300, 1) // Низкий метаболизм и восприятие для экономии энергии
        val organism = Organism(1, Position(5, 5), genome)
        
        // Первые 20 тиков - фаза роста (если организм не умрет от истощения энергии)
        repeat(15) {
            organism.tick(simulation, random)
            if (!organism.isDead()) {
                assertEquals(Organism.Phase.GROWTH, organism.phase)
            }
        }
    }
    
    @Test
    fun `test organism energy consumption`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = Organism(1, Position(5, 5), genome)
        val initialEnergy = organism.energy
        
        organism.tick(simulation, random)
        
        // Энергия должна уменьшиться на метаболизм + стоимость восприятия + стоимость движения
        val expectedEnergyLoss = genome.metabolism + (genome.perception - 1) * 2 + 10
        assertEquals(initialEnergy - expectedEnergyLoss, organism.energy)
    }
    
    @Test
    fun `test organism food consumption`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = Organism(1, Position(5, 5), genome)
        
        // Добавляем еду в позицию организма
        simulation.food.add(Position(5, 5))
        val initialEnergy = organism.energy
        
        organism.tick(simulation, random)
        
        // Энергия должна увеличиться на эффективность пищеварения
        val expectedEnergyGain = genome.digestionEfficiency
        val energyLoss = genome.metabolism + (genome.perception - 1) * 2 + 10
        assertEquals(initialEnergy - energyLoss + expectedEnergyGain, organism.energy)
        
        // Еда должна быть съедена
        assertFalse(simulation.food.contains(Position(5, 5)))
    }
    
    @Test
    fun `test organism movement`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val initialPosition = Position(5, 5)
        val organism = Organism(1, initialPosition, genome)
        
        organism.tick(simulation, random)
        
        // Позиция должна измениться (организм движется)
        assertNotEquals(initialPosition, organism.position)
    }
    
    @Test
    fun `test organism movement within bounds`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = Organism(1, Position(0, 0), genome)
        
        repeat(10) {
            organism.tick(simulation, random)
            
            // Позиция должна оставаться в границах поля
            assertTrue("X should be >= 0", organism.position.x >= 0)
            assertTrue("X should be < 10", organism.position.x < 10)
            assertTrue("Y should be >= 0", organism.position.y >= 0)
            assertTrue("Y should be < 10", organism.position.y < 10)
        }
    }
    
    @Test
    fun `test organism reproduction age threshold`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = Organism(1, Position(5, 5), genome)
        
        // До достижения порога размножения
        repeat(50) {
            organism.tick(simulation, random)
            assertNotEquals(Organism.Phase.REPRODUCTION, organism.phase)
        }
        
        // После достижения порога размножения
        repeat(60) {
            organism.tick(simulation, random)
        }
        
        // Фаза размножения может наступить
        assertTrue(organism.phase == Organism.Phase.REPRODUCTION || 
                  organism.phase == Organism.Phase.GROWTH || 
                  organism.phase == Organism.Phase.DEATH)
    }
    
    @Test
    fun `test organism reproduction energy threshold`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = Organism(1, Position(5, 5), genome)
        
        // Устанавливаем возраст выше порога размножения, но низкую энергию
        organism.age = 150
        organism.energy = 50 // Ниже порога размножения
        
        organism.tick(simulation, random)
        
        // Не должно быть фазы размножения из-за недостатка энергии
        assertNotEquals(Organism.Phase.REPRODUCTION, organism.phase)
    }
    
    @Test
    fun `test organism perception cost`() {
        val lowPerceptionGenome = Genome(2, 3, 20, 300, 1)
        val highPerceptionGenome = Genome(2, 3, 20, 300, 5)
        
        val lowPerceptionOrg = Organism(1, Position(5, 5), lowPerceptionGenome)
        val highPerceptionOrg = Organism(2, Position(5, 5), highPerceptionGenome)
        
        val initialEnergy = 100
        
        lowPerceptionOrg.tick(simulation, random)
        highPerceptionOrg.tick(simulation, random)
        
        // Организм с высоким восприятием должен тратить больше энергии
        val lowPerceptionEnergyLoss = initialEnergy - lowPerceptionOrg.energy
        val highPerceptionEnergyLoss = initialEnergy - highPerceptionOrg.energy
        
        assertTrue("High perception should cost more energy", 
                  highPerceptionEnergyLoss > lowPerceptionEnergyLoss)
    }
    
    @Test
    fun `test organism age increment`() {
        val genome = Genome(2, 3, 20, 300, 4)
        val organism = Organism(1, Position(5, 5), genome)
        
        val initialAge = organism.age
        
        organism.tick(simulation, random)
        
        assertEquals(initialAge + 1, organism.age)
    }
    
    @Test
    fun `test dead organism does not tick`() {
        val genome = Genome(2, 100, 20, 300, 4) // Высокий метаболизм для быстрой смерти
        val organism = Organism(1, Position(5, 5), genome)
        
        // Убиваем организм
        organism.tick(simulation, random)
        assertTrue(organism.isDead())
        
        val deadPosition = organism.position.copy()
        val deadEnergy = organism.energy
        
        // Пытаемся тикать мертвый организм
        organism.tick(simulation, random)
        
        // Позиция и энергия не должны измениться
        assertEquals(deadPosition, organism.position)
        assertEquals(deadEnergy, organism.energy)
    }
} 