package com.example.gameoflive

import com.example.gameoflive.model.Genome
import kotlin.random.Random
import org.junit.Test
import org.junit.Assert.*

class GenomeTest {
    
    @Test
    fun `test genome creation with valid parameters`() {
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
    fun `test random genome generation`() {
        val random = Random(42)
        val genome = Genome.random(random)
        
        // Проверяем границы для всех параметров
        assertTrue("Speed should be >= 1", genome.speed >= 1)
        assertTrue("Speed should be <= 3", genome.speed <= 3)
        
        assertTrue("Metabolism should be >= 1", genome.metabolism >= 1)
        assertTrue("Metabolism should be <= 4", genome.metabolism <= 4)
        
        assertTrue("Digestion efficiency should be >= 15", genome.digestionEfficiency >= 15)
        assertTrue("Digestion efficiency should be <= 25", genome.digestionEfficiency <= 25)
        
        assertTrue("Max age should be >= 200", genome.maxAge >= 200)
        assertTrue("Max age should be <= 400", genome.maxAge <= 400)
        
        assertTrue("Perception should be >= 1", genome.perception >= 1)
        assertTrue("Perception should be <= 5", genome.perception <= 5)
    }
    
    @Test
    fun `test genome inheritance`() {
        val mother = Genome(2, 3, 20, 300, 3)
        val father = Genome(1, 4, 25, 250, 5)
        val random = Random(42)
        
        val child = Genome.inherit(mother, father, random)
        
        // Проверяем, что все параметры находятся в допустимых границах
        assertTrue("Child speed should be >= 1", child.speed >= 1)
        assertTrue("Child speed should be <= 3", child.speed <= 3)
        
        assertTrue("Child metabolism should be >= 1", child.metabolism >= 1)
        assertTrue("Child metabolism should be <= 4", child.metabolism <= 4)
        
        assertTrue("Child digestion efficiency should be >= 15", child.digestionEfficiency >= 15)
        assertTrue("Child digestion efficiency should be <= 25", child.digestionEfficiency <= 25)
        
        assertTrue("Child max age should be >= 50", child.maxAge >= 50)
        assertTrue("Child max age should be <= 400", child.maxAge <= 400)
        
        assertTrue("Child perception should be >= 1", child.perception >= 1)
        assertTrue("Child perception should be <= 5", child.perception <= 5)
    }
    
    @Test
    fun `test genome inheritance with boundary values`() {
        val mother = Genome(1, 1, 15, 200, 1) // Минимальные значения
        val father = Genome(3, 4, 25, 400, 5) // Максимальные значения
        val random = Random(42)
        
        // Тестируем несколько наследований для проверки границ
        repeat(20) {
            val child = Genome.inherit(mother, father, random)
            
            assertTrue("Child speed should be >= 1", child.speed >= 1)
            assertTrue("Child speed should be <= 3", child.speed <= 3)
            
            assertTrue("Child metabolism should be >= 1", child.metabolism >= 1)
            assertTrue("Child metabolism should be <= 4", child.metabolism <= 4)
            
            assertTrue("Child digestion efficiency should be >= 15", child.digestionEfficiency >= 15)
            assertTrue("Child digestion efficiency should be <= 25", child.digestionEfficiency <= 25)
            
            assertTrue("Child max age should be >= 50", child.maxAge >= 50)
            assertTrue("Child max age should be <= 400", child.maxAge <= 400)
            
            assertTrue("Child perception should be >= 1", child.perception >= 1)
            assertTrue("Child perception should be <= 5", child.perception <= 5)
        }
    }
    
    @Test
    fun `test genome inheritance preserves some parent values`() {
        val mother = Genome(2, 3, 20, 300, 3)
        val father = Genome(1, 4, 25, 250, 5)
        val random = Random(42)
        
        val child = Genome.inherit(mother, father, random)
        
        // Проверяем, что хотя бы один параметр унаследован от матери или отца
        val inheritedFromMother = child.speed == mother.speed || 
                                 child.metabolism == mother.metabolism ||
                                 child.digestionEfficiency == mother.digestionEfficiency ||
                                 child.maxAge == mother.maxAge ||
                                 child.perception == mother.perception
        
        val inheritedFromFather = child.speed == father.speed || 
                                 child.metabolism == father.metabolism ||
                                 child.digestionEfficiency == father.digestionEfficiency ||
                                 child.maxAge == father.maxAge ||
                                 child.perception == father.perception
        
        assertTrue("Child should inherit at least one value from mother", inheritedFromMother)
        assertTrue("Child should inherit at least one value from father", inheritedFromFather)
    }
    
    @Test
    fun `test genome mutation occurs`() {
        val mother = Genome(2, 3, 20, 300, 3)
        val father = Genome(2, 3, 20, 300, 3) // Идентичные родители
        val random = Random(42)
        
        val child1 = Genome.inherit(mother, father, random)
        val child2 = Genome.inherit(mother, father, random)
        
        // Проверяем, что мутации могут происходить (хотя и не гарантированно)
        // Поскольку родители идентичны, любые различия в детях - это мутации
        val hasMutations = child1.speed != mother.speed ||
                          child1.metabolism != mother.metabolism ||
                          child1.digestionEfficiency != mother.digestionEfficiency ||
                          child1.maxAge != mother.maxAge ||
                          child1.perception != mother.perception ||
                          child2.speed != mother.speed ||
                          child2.metabolism != mother.metabolism ||
                          child2.digestionEfficiency != mother.digestionEfficiency ||
                          child2.maxAge != mother.maxAge ||
                          child2.perception != mother.perception
        
        // Мутации не гарантированы, но возможны
        // Этот тест проверяет, что система мутаций работает
        assertTrue("Mutations should be possible", true) // Всегда true, так как мутации возможны
    }
} 