package com.example.gameoflive.domain.usecase

import com.example.gameoflive.model.Genome
import com.example.gameoflive.model.Organism
import javax.inject.Inject

class CalculateMedianGenomeUseCase @Inject constructor() {
    operator fun invoke(organisms: List<Organism>): Genome {
        if (organisms.isEmpty()) {
            return Genome(0, 0, 0, 0, 0)
        }
        val speeds = organisms.map { it.genome.speed }.sorted()
        val metabolisms = organisms.map { it.genome.metabolism }.sorted()
        val digestions = organisms.map { it.genome.digestionEfficiency }.sorted()
        val maxAges = organisms.map { it.genome.maxAge }.sorted()
        val perceptions = organisms.map { it.genome.perception }.sorted()
        val medianSpeed = speeds[speeds.size / 2]
        val medianMetabolism = metabolisms[metabolisms.size / 2]
        val medianDigestion = digestions[digestions.size / 2]
        val medianMaxAge = maxAges[maxAges.size / 2]
        val medianPerception = perceptions[perceptions.size / 2]
        return Genome(
            speed = medianSpeed,
            metabolism = medianMetabolism,
            digestionEfficiency = medianDigestion,
            maxAge = medianMaxAge,
            perception = medianPerception
        )
    }
} 