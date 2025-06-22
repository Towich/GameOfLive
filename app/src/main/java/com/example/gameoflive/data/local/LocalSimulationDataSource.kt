package com.example.gameoflive.data.local

import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.model.Simulation

/**
 * Источник данных, работающий с файловой системой/БД устройства.
 * Все операции предполагают работу на IO-потоке.
 */
interface LocalSimulationDataSource {
    suspend fun save(name: String, simulation: Simulation)
    suspend fun load(fileName: String): Simulation?
    suspend fun list(): List<SaveInfo>
    suspend fun delete(fileName: String): Boolean
} 