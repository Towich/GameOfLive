package com.example.gameoflive

import com.example.gameoflive.model.Genome
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*

class GenomePerceptionTest {
    
    @Test
    fun `test perception parameter in genome`() {
        // Тестируем создание генома с параметром perception
        val genome = Genome(
            speed = 2,
            metabolism = 3,
            digestionEfficiency = 20,
            maxAge = 300,
            perception = 4
        )
        
        assertEquals(2, genome.speed)
        assertEquals(3, genome.metabolism)
        assertEquals(20, genome.digestionEfficiency)
        assertEquals(300, genome.maxAge)
        assertEquals(4, genome.perception)
    }
    
    @Test
    fun `test random genome generation includes perception`() {
        val random = Random(42) // Фиксированный seed для воспроизводимости
        val genome = Genome.random(random)
        
        // Проверяем, что perception находится в допустимом диапазоне
        assertTrue("Perception should be >= 1", genome.perception >= 1)
        assertTrue("Perception should be <= 5", genome.perception <= 5)
        
        // Проверяем, что все параметры установлены
        assertTrue("Speed should be > 0", genome.speed > 0)
        assertTrue("Metabolism should be > 0", genome.metabolism > 0)
        assertTrue("Digestion efficiency should be > 0", genome.digestionEfficiency > 0)
        assertTrue("Max age should be > 0", genome.maxAge > 0)
    }
    
    @Test
    fun `test genome inheritance includes perception`() {
        val mother = Genome(2, 3, 20, 300, 3)
        val father = Genome(1, 4, 25, 250, 5)
        val random = Random(42)
        
        val child = Genome.inherit(mother, father, random)
        
        // Проверяем, что perception унаследован
        assertTrue("Child perception should be >= 1", child.perception >= 1)
        assertTrue("Child perception should be <= 5", child.perception <= 5)
        
        // Проверяем, что все параметры унаследованы
        assertTrue("Child speed should be > 0", child.speed > 0)
        assertTrue("Child metabolism should be > 0", child.metabolism > 0)
        assertTrue("Child digestion efficiency should be > 0", child.digestionEfficiency > 0)
        assertTrue("Child max age should be > 0", child.maxAge > 0)
    }
    
    @Test
    fun `test perception mutation bounds`() {
        val mother = Genome(2, 3, 20, 300, 1) // Минимальное восприятие
        val father = Genome(1, 4, 25, 250, 5) // Максимальное восприятие
        val random = Random(42)
        
        // Тестируем несколько наследований
        repeat(10) {
            val child = Genome.inherit(mother, father, random)
            assertTrue("Child perception should be >= 1", child.perception >= 1)
            assertTrue("Child perception should be <= 5", child.perception <= 5)
        }
    }
} 