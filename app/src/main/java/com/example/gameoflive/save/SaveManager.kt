package com.example.gameoflive.save

import android.content.Context
import com.example.gameoflive.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

@Singleton
class SaveManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SAVE_DIR = "saves"
    private val EXT = ".json"
    private val json = Json { prettyPrint = true }

    fun saveSimulation(name: String, simulation: Simulation) {
        val snapshot = SimulationSnapshot.fromSimulation(name, simulation)
        val dir = File(context.filesDir, SAVE_DIR)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$name$EXT")
        file.writeText(json.encodeToString(snapshot))
    }

    fun listSaves(): List<com.example.gameoflive.domain.model.SaveInfo> {
        val dir = File(context.filesDir, SAVE_DIR)
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.extension == "json" }?.mapNotNull { file ->
            runCatching {
                val snapshot: SimulationSnapshot = json.decodeFromString(file.readText())
                val median = calculateMedianGenome(snapshot.organisms)
                // Для старых сохранений без saveDate используем время модификации файла
                val saveDate = if (snapshot.saveDate == 0L) file.lastModified() else snapshot.saveDate
                com.example.gameoflive.domain.model.SaveInfo(
                    fileName = file.name,
                    name = snapshot.name,
                    tickCounter = snapshot.tickCounter,
                    organismCount = snapshot.organisms.size,
                    medianGenome = median,
                    saveDate = saveDate
                )
            }.getOrNull()
        }?.sortedByDescending { it.saveDate } ?: emptyList() // Сортируем по дате (новые сверху)
    }

    fun loadSimulation(fileName: String): Simulation? {
        val dir = File(context.filesDir, SAVE_DIR)
        val file = File(dir, fileName)
        if (!file.exists()) return null
        val snapshot: SimulationSnapshot = json.decodeFromString(file.readText())
        return snapshot.toSimulation()
    }

    fun deleteSave(fileName: String): Boolean {
        val dir = File(context.filesDir, SAVE_DIR)
        val file = File(dir, fileName)
        return file.exists() && file.delete()
    }

    /* ===== Snapshots ===== */

    @Serializable
    data class OrganismSnapshot(
        val id: Int,
        val position: Position,
        val genome: Genome,
        val sex: Sex,
        val energy: Int,
        val age: Int
    )

    @Serializable
    data class SimulationSnapshot(
        val name: String,
        val tickCounter: Long,
        val width: Int,
        val height: Int,
        val nextId: Int,
        val organisms: List<OrganismSnapshot>,
        val food: List<Position>,
        val saveDate: Long = System.currentTimeMillis()
    ) {
        companion object {
            fun fromSimulation(name: String, sim: Simulation): SimulationSnapshot {
                val orgs = sim.organisms.map {
                    OrganismSnapshot(
                        id = it.id,
                        position = it.position.copy(),
                        genome = it.genome,
                        sex = it.sex,
                        energy = it.energy,
                        age = it.age
                    )
                }
                return SimulationSnapshot(
                    name = name,
                    tickCounter = sim.tickCounter,
                    width = sim.width,
                    height = sim.height,
                    nextId = sim.nextId,
                    organisms = orgs,
                    food = sim.food.toList(),
                    saveDate = System.currentTimeMillis()
                )
            }
        }

        fun toSimulation(): Simulation {
            val sim = Simulation(width = width, height = height)
            sim.tickCounter = tickCounter
            sim.nextId = nextId
            sim.food.addAll(food.toMutableSet())
            organisms.forEach { o ->
                val org = Organism(
                    id = o.id,
                    position = o.position.copy(),
                    genome = o.genome,
                    sex = o.sex,
                    energy = o.energy,
                    age = o.age
                )
                sim.organisms += org
            }
            return sim
        }
    }

    /* ===== Helpers ===== */

    private fun calculateMedianGenome(list: List<OrganismSnapshot>): Genome {
        fun median(v: List<Int>): Int = if (v.isEmpty()) 0 else v.sorted()[v.size / 2]
        return Genome(
            speed = median(list.map { it.genome.speed }),
            metabolism = median(list.map { it.genome.metabolism }),
            digestionEfficiency = median(list.map { it.genome.digestionEfficiency }),
            maxAge = median(list.map { it.genome.maxAge })
        )
    }
} 