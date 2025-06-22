package com.example.gameoflive.data.local

import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.model.Simulation
import com.example.gameoflive.save.SaveManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Обёртка над SaveManager, теперь SaveManager внедряется через DI.
 */
class LocalSimulationDataSourceImpl @Inject constructor(
    private val saveManager: SaveManager
) : LocalSimulationDataSource {

    override suspend fun save(name: String, simulation: Simulation) = withContext(Dispatchers.IO) {
        saveManager.saveSimulation(name, simulation)
    }

    override suspend fun load(fileName: String): Simulation? = withContext(Dispatchers.IO) {
        saveManager.loadSimulation(fileName)
    }

    override suspend fun list(): List<SaveInfo> = withContext(Dispatchers.IO) {
        saveManager.listSaves().map {
            SaveInfo(
                fileName = it.fileName,
                name = it.name,
                tickCounter = it.tickCounter,
                organismCount = it.organismCount,
                medianGenome = it.medianGenome
            )
        }
    }

    override suspend fun delete(fileName: String): Boolean = withContext(Dispatchers.IO) {
        saveManager.deleteSave(fileName)
    }
} 