package com.example.gameoflive.data.remote

import com.example.gameoflive.model.Simulation

class RemoteSimulationDataSourceStub : RemoteSimulationDataSource {
    override suspend fun upload(name: String, simulation: Simulation) {
        // no-op
    }

    override suspend fun download(id: String): Simulation {
        throw NotImplementedError("Remote download is not implemented")
    }
} 