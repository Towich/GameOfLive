package com.example.gameoflive.data.remote

import com.example.gameoflive.model.Simulation

/**
 * Заглушка для работы с сервером/облаком.
 */
interface RemoteSimulationDataSource {
    suspend fun upload(name: String, simulation: Simulation)
    suspend fun download(id: String): Simulation
} 