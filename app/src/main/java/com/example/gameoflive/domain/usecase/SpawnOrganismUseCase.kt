package com.example.gameoflive.domain.usecase

import com.example.gameoflive.GameConfig
import com.example.gameoflive.model.Position
import com.example.gameoflive.model.Simulation
import kotlin.random.Random

class SpawnOrganismUseCase {
    operator fun invoke(simulation: Simulation, count: Int = 1) {
        repeat(count) {
            val pos = Position(
                Random.nextInt(simulation.width),
                Random.nextInt(simulation.height)
            )
            if (simulation.isCellFree(pos)) {
                simulation.spawnOrganism(
                    GameConfig.randomGenome(),
                    pos
                )
            }
        }
    }
} 