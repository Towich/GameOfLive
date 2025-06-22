package com.example.gameoflive

import com.example.gameoflive.model.Genome
import kotlin.random.Random

object GameConfig {
    // Размер игрового поля
    const val FIELD_WIDTH = 60
    const val FIELD_HEIGHT = 60

    // Организм
    const val INITIAL_ENERGY = 100
    const val GROWTH_AGE_THRESHOLD = 20
    const val REPRODUCTION_AGE_THRESHOLD = 100
    const val REPRODUCTION_ENERGY_THRESHOLD = 120
    const val REPRODUCTION_ENERGY_COST = 40
    const val REPRODUCTION_COOLDOWN_TICKS = 10
    const val MOVEMENT_ENERGY_COST = 10

    // Симуляция
    const val FOOD_SPAWN_CHANCE = 10   // шанс из 1000 для каждой клетки
    const val PARTNER_SEARCH_RADIUS = 1
    const val FAST_FORWARD_TICKS = 100
    const val SPAWN_ORGANISM_COUNT = 10

    // Геном
    const val MUTATION_CHANCE = 5
    const val MUTATION_DELTA = 1

    const val GENOME_SPEED_MIN = 1
    const val GENOME_SPEED_MAX = 3
    const val GENOME_METABOLISM_MIN = 1
    const val GENOME_METABOLISM_MAX = 4
    const val GENOME_DIGESTION_MIN = 15
    const val GENOME_DIGESTION_MAX = 25
    const val GENOME_MAX_AGE_MIN = 200
    const val GENOME_MAX_AGE_MAX = 400

    fun randomGenome(random: Random = Random): Genome = Genome(
        speed = random.nextInt(GENOME_SPEED_MIN, GENOME_SPEED_MAX),
        metabolism = random.nextInt(GENOME_METABOLISM_MIN, GENOME_METABOLISM_MAX),
        digestionEfficiency = random.nextInt(GENOME_DIGESTION_MIN, GENOME_DIGESTION_MAX),
        maxAge = random.nextInt(GENOME_MAX_AGE_MIN, GENOME_MAX_AGE_MAX)
    )
} 