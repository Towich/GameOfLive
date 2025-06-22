package com.example.gameoflive.data.repository

import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.model.Simulation
import kotlinx.coroutines.flow.Flow

interface SimulationRepository {
    suspend fun saveSimulation(name: String, simulation: Simulation)
    suspend fun loadSimulation(fileName: String): Simulation?
    suspend fun listSaves(): List<SaveInfo>
    suspend fun deleteSave(fileName: String): Boolean

    /**
     * Возвращает Flow, публикующий состояние симуляции каждый тик.
     * Репозиторий сам запускает тики на фоне.
     */
    fun observeSimulation(simulation: Simulation): Flow<Simulation>
    
    /**
     * Принудительно обновляет UI после изменений симуляции.
     */
    suspend fun triggerUpdate()
} 