package com.example.gameoflive

import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.model.Genome
import org.junit.Test
import org.junit.Assert.*

class SaveInfoTest {
    
    @Test
    fun testGetFormattedDate() {
        // Создаем SaveInfo с известной датой (1 января 2024, 12:00)
        val saveInfo = SaveInfo(
            fileName = "test.json",
            name = "Test Save",
            tickCounter = 100,
            organismCount = 50,
            medianGenome = Genome(speed = 5, metabolism = 3, digestionEfficiency = 7, maxAge = 100, perception = 3),
            minGenome = Genome(speed = 1, metabolism = 1, digestionEfficiency = 1, maxAge = 50, perception = 1),
            maxGenome = Genome(speed = 10, metabolism = 10, digestionEfficiency = 10, maxAge = 200, perception = 5),
            saveDate = 1704110400000L // 1 января 2024, 12:00 UTC
        )
        
        val formattedDate = saveInfo.getFormattedDate()
        
        // Проверяем, что дата форматируется корректно
        assertTrue("Формат даты должен содержать день, месяц, год и время", 
                  formattedDate.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}")))
        
        // Проверяем, что дата не пустая
        assertFalse("Форматированная дата не должна быть пустой", formattedDate.isEmpty())
    }
    
    @Test
    fun testSaveInfoProperties() {
        val saveInfo = SaveInfo(
            fileName = "test.json",
            name = "Test Save",
            tickCounter = 100,
            organismCount = 50,
            medianGenome = Genome(speed = 5, metabolism = 3, digestionEfficiency = 7, maxAge = 100, perception = 3),
            minGenome = Genome(speed = 1, metabolism = 1, digestionEfficiency = 1, maxAge = 50, perception = 1),
            maxGenome = Genome(speed = 10, metabolism = 10, digestionEfficiency = 10, maxAge = 200, perception = 5),
            saveDate = System.currentTimeMillis()
        )
        
        assertEquals("test.json", saveInfo.fileName)
        assertEquals("Test Save", saveInfo.name)
        assertEquals(100L, saveInfo.tickCounter)
        assertEquals(50, saveInfo.organismCount)
        assertEquals(5, saveInfo.medianGenome.speed)
        assertEquals(1, saveInfo.minGenome.speed)
        assertEquals(10, saveInfo.maxGenome.speed)
        assertTrue("saveDate должен быть положительным числом", saveInfo.saveDate > 0)
    }
} 