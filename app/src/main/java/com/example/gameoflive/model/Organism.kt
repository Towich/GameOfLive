package com.example.gameoflive.model

import android.util.Log
import kotlin.random.Random
import com.example.gameoflive.GameConfig

/** Положение на игровом поле */
data class Position(var x: Int, var y: Int) {
    fun copy() = Position(x, y)
}

enum class Sex { MALE, FEMALE }

class Organism(
    val id: Int,
    var position: Position,
    var genome: Genome,
    val sex: Sex = if (Random.nextBoolean()) Sex.MALE else Sex.FEMALE,
    var energy: Int = 100,
    var age: Int = 0,
    private var lastReproductionTick: Long = -1000,
) {
    /** Фаза жизни */
    enum class Phase { BIRTH, GROWTH, REPRODUCTION, DEATH }

    var phase: Phase = Phase.BIRTH
        private set

    fun isDead(): Boolean = phase == Phase.DEATH

    /** Один шаг симуляции для конкретного организма. */
    fun tick(sim: Simulation, random: Random = Random) {
        // Log.d("Organism", "Tick id=$id phase=$phase pos=$position energy=$energy age=$age") // убрано по просьбе
        if (phase == Phase.DEATH) return

        age++
        if (age > genome.maxAge) {
            phase = Phase.DEATH
            Log.d("Organism", "Organism $id died (old age)")
            return
        }

        // тратим энергию на поддержание жизни
        energy -= genome.metabolism
        if (energy <= 0) {
            phase = Phase.DEATH
            Log.d("Organism", "Organism $id died (energy depleted)")
            return
        }

        // рост в раннем возрасте
        if (age < GameConfig.GROWTH_AGE_THRESHOLD) {
            phase = Phase.GROWTH
        }

        // поедание еды, если она есть в клетке
        if (sim.consumeFoodAt(position)) {
            energy += genome.digestionEfficiency
            Log.d("Organism", "Organism $id ate food; energy=$energy")
        }

        // попытка спаривания
        if (age > GameConfig.REPRODUCTION_AGE_THRESHOLD && energy > GameConfig.REPRODUCTION_ENERGY_THRESHOLD &&
            sim.tickCounter - lastReproductionTick >= GameConfig.REPRODUCTION_COOLDOWN_TICKS
        ) {
            phase = Phase.REPRODUCTION
            attemptReproduce(sim, random)
        }

        // поиск ближайшей еды и движение к ней
        moveTowardsFood(sim, random)
        // Log.d("Organism", "Organism $id moved to $position; energy=$energy") // движение не логируем
    }

    private fun attemptReproduce(sim: Simulation, random: Random) {
        val partner = sim.findPartnerNearby(this)
        if (partner != null && this.id < partner.id && sex != partner.sex) { // только один из пары создаёт ребёнка
            energy -= GameConfig.REPRODUCTION_ENERGY_COST // трата энергии
            partner.energy -= GameConfig.REPRODUCTION_ENERGY_COST
            lastReproductionTick = sim.tickCounter
            partner.lastReproductionTick = sim.tickCounter

            val childGenome = Genome.inherit(this.genome, partner.genome, random)
            val freePos = sim.findFreeAdjacent(position)
            if (freePos != null) {
                sim.spawnOrganism(childGenome, freePos, random)
            }
        }
    }

    private fun moveTowardsFood(sim: Simulation, random: Random) {
        val target = sim.findNearestFood(position, genome.speed)
        val newPos = if (target != null) {
            stepTowards(position, target)
        } else {
            // случайное блуждание
            val dx = random.nextInt(-1, 2)
            val dy = random.nextInt(-1, 2)
            Position(position.x + dx, position.y + dy)
        }
        val clamped = sim.clampPosition(newPos)
        if (sim.isCellFree(clamped)) {
            position = clamped
        }
        energy -= GameConfig.MOVEMENT_ENERGY_COST // стоимость движения
    }

    private fun stepTowards(from: Position, to: Position): Position {
        val dx = to.x.compareTo(from.x)
        val dy = to.y.compareTo(from.y)
        return Position(
            from.x + dx.coerceIn(-genome.speed, genome.speed),
            from.y + dy.coerceIn(-genome.speed, genome.speed)
        )
    }
} 