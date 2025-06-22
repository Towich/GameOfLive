package com.example.gameoflive.data.repository

import com.example.gameoflive.data.local.LocalSimulationDataSource
import com.example.gameoflive.data.remote.RemoteSimulationDataSource
import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.model.Simulation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map

class SimulationRepositoryImpl(
    private val local: LocalSimulationDataSource,
    private val remote: RemoteSimulationDataSource
) : SimulationRepository {
    
    // Flow для принудительного обновления UI
    private val updateTrigger = MutableSharedFlow<Unit>()
    
    override suspend fun saveSimulation(name: String, simulation: Simulation) {
        local.save(name, simulation)
        // Можно отправить на сервер параллельно, если нужно
        // remote.upload(name, simulation)
    }

    override suspend fun loadSimulation(fileName: String): Simulation? {
        return local.load(fileName)
    }

    override suspend fun listSaves(): List<SaveInfo> = local.list()

    override suspend fun deleteSave(fileName: String): Boolean = local.delete(fileName)

    override fun observeSimulation(simulation: Simulation): Flow<Simulation> {
        val tickFlow = flow {
            while (true) {
                emit(simulation.copyDeep())
                delay(300L)
                simulation.tick()
            }
        }.flowOn(Dispatchers.Default)
        
        val updateFlow = updateTrigger.map { simulation.copyDeep() }
        
        return merge(tickFlow, updateFlow)
    }
    
    // Метод для принудительного обновления UI
    override suspend fun triggerUpdate() {
        updateTrigger.emit(Unit)
    }
} 