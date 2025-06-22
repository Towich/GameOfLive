package com.example.gameoflive.data.local

import android.content.Context
import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.model.Simulation
import com.example.gameoflive.save.SaveManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Обёртка над существующим [SaveManager]. В дальнейшем код SaveManager можно перенести напрямую,
 * но пока так проще не ломать существующий UI.
 */
class LocalSimulationDataSourceImpl(
    private val context: Context
) : LocalSimulationDataSource {

    override suspend fun save(name: String, simulation: Simulation) = withContext(Dispatchers.IO) {
        SaveManager.saveSimulation(context, name, simulation)
    }

    override suspend fun load(fileName: String): Simulation? = withContext(Dispatchers.IO) {
        SaveManager.loadSimulation(context, fileName)
    }

    override suspend fun list(): List<SaveInfo> = withContext(Dispatchers.IO) {
        // преобразуем в доменные модели
        SaveManager.listSaves(context).map {
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
        SaveManager.deleteSave(context, fileName)
    }
} 