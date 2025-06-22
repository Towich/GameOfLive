package com.example.gameoflive.model

import kotlin.math.abs
import kotlin.random.Random
import android.util.Log
import com.example.gameoflive.GameConfig

class Simulation(
    val width: Int = GameConfig.FIELD_WIDTH,
    val height: Int = GameConfig.FIELD_HEIGHT,
    private val random: Random = Random,
    private val onEvent: (String) -> Unit = {},
) {
    var nextId: Int = 0

    /** Пищу храним как набор позиций для простоты. */
    val food: MutableSet<Position> = mutableSetOf()

    /** Все живые организмы */
    val organisms: MutableList<Organism> = mutableListOf()

    /** Счётчик тиков, полезен для отрисовки */
    var tickCounter: Long = 0

    fun spawnOrganism(genome: Genome, position: Position, random: Random = Random): Organism {
        val org = Organism(id = nextId++, position = position, genome = genome, sex = if (random.nextBoolean()) Sex.MALE else Sex.FEMALE)
        organisms += org
        Log.d("Simulation", "Spawned organism id=${org.id} at $position genome=$genome")
        onEvent("Родился организм #${org.id} на (${position.x},${position.y})")
        return org
    }

    private fun spawnRandomFood() {
        val before = food.size
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (random.nextInt(1000) < GameConfig.FOOD_SPAWN_CHANCE) {
                    food += Position(x, y)
                }
            }
        }
        val added = food.size - before
        if (added > 0) Log.d("Simulation", "Spawned $added food items (total=${food.size})")
    }

    fun tick() {
        tickCounter++
        Log.d("Simulation", "=== Tick $tickCounter === organisms=${organisms.size}")

        // добавляем новую еду случайно
        spawnRandomFood()

        // создаём снимок списка, чтобы избежать ConcurrentModificationException при добавлении новых организмов во время итерации
        for (org in organisms.toList()) {
            org.tick(this, random)
        }

        // соберём умерших
        val dead = organisms.filter { it.isDead() }
        if (dead.isNotEmpty()) {
            dead.forEach { onEvent("Организм #${it.id} умер") }
            organisms.removeAll(dead.toSet())
            Log.d("Simulation", "Removed ${dead.size} dead organisms; now size=${organisms.size}")
        }
    }

    /* ==== API, используемый организмами ==== */

    fun consumeFoodAt(pos: Position): Boolean = if (food.remove(pos)) true else false

    fun isCellFree(pos: Position): Boolean = organisms.none { it.position.x == pos.x && it.position.y == pos.y }

    fun clampPosition(pos: Position): Position = Position(pos.x.coerceIn(0, width - 1), pos.y.coerceIn(0, height - 1))

    fun findPartnerNearby(seeker: Organism): Organism? {
        return organisms.firstOrNull { it !== seeker && abs(it.position.x - seeker.position.x) <= GameConfig.PARTNER_SEARCH_RADIUS && abs(it.position.y - seeker.position.y) <= GameConfig.PARTNER_SEARCH_RADIUS }
    }

    fun findFreeAdjacent(pos: Position): Position? {
        val dirs = listOf(
            Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1),
            Position(-1, -1), Position(-1, 1), Position(1, -1), Position(1, 1)
        )
        return dirs.map { Position(pos.x + it.x, pos.y + it.y) }
            .map { clampPosition(it) }
            .firstOrNull { isCellFree(it) }
    }

    fun findNearestFood(from: Position, maxDistance: Int): Position? {
        var nearest: Position? = null
        var dist = Int.MAX_VALUE
        for (foodPos in food) {
            val d = abs(foodPos.x - from.x) + abs(foodPos.y - from.y)
            if (d < dist && d <= maxDistance) {
                dist = d
                nearest = foodPos
            }
        }
        return nearest
    }

    fun copyDeep(): Simulation {
        val newSim = Simulation(width, height)
        newSim.nextId = this.nextId
        newSim.tickCounter = this.tickCounter
        newSim.food.addAll(this.food.map { it.copy() })
        newSim.organisms.addAll(this.organisms.map { it.copyOrganism() })
        return newSim
    }
}

// --- Копирование для реактивного UI ---

fun Simulation.copyDeep(): Simulation {
    val newSim = Simulation(width, height)
    newSim.nextId = this.nextId
    newSim.tickCounter = this.tickCounter
    newSim.food.addAll(this.food.map { it.copy() })
    newSim.organisms.addAll(this.organisms.map { it.copyOrganism() })
    return newSim
}

fun Organism.copyOrganism(): Organism = Organism(
    id = this.id,
    position = this.position.copy(),
    genome = this.genome,
    sex = this.sex,
    energy = this.energy,
    age = this.age
) 