package com.example.gameoflive.model

import android.util.Log
import kotlin.random.Random
import com.example.gameoflive.GameConfig
import kotlinx.serialization.Serializable

/**
 * Простая модель генома, который отвечает за основные параметры организма.
 * Все параметры выражены в целых числах для простоты вычислений.
 */
@Serializable
data class Genome(
    val speed: Int,               // количество клеток, которое организм может пройти за один тик
    val metabolism: Int,          // энергия, затрачиваемая каждый тик на поддержание жизнедеятельности
    val digestionEfficiency: Int, // сколько энергии организм получает из одной еды
    val maxAge: Int,              // максимальный возраст организма в тиках
    val perception: Int           // радиус, в котором организм может видеть еду и партнеров
) {

    companion object {
        fun random(random: Random = Random): Genome {
            val genome = GameConfig.randomGenome(random)
            // Log.d("Genome", "Random genome generated: $genome")
            return genome
        }

        /**
         * Применить мутацию к значению с заданными границами
         */
        private fun mutateValue(
            value: Int, 
            random: Random, 
            minValue: Int, 
            maxValue: Int = Int.MAX_VALUE
        ): Int {
            return if (random.nextInt(100) < GameConfig.MUTATION_CHANCE) {
                (value + random.nextInt(-GameConfig.MUTATION_DELTA, GameConfig.MUTATION_DELTA + 1))
                    .coerceIn(minValue, maxValue)
            } else {
                value
            }
        }

        /**
         * Создать новый геном на основе родительских геномов с возможной мутацией.
         */
        fun inherit(mother: Genome, father: Genome, random: Random = Random): Genome {
            // берём случайным образом значения от родителей
            var speed = if (random.nextBoolean()) mother.speed else father.speed
            var metabolism = if (random.nextBoolean()) mother.metabolism else father.metabolism
            var digestionEfficiency =
                if (random.nextBoolean()) mother.digestionEfficiency else father.digestionEfficiency
            var maxAge = if (random.nextBoolean()) mother.maxAge else father.maxAge
            var perception = if (random.nextBoolean()) mother.perception else father.perception

            // применяем мутации
            speed = mutateValue(speed, random, 1)
            metabolism = mutateValue(metabolism, random, 1)
            digestionEfficiency = mutateValue(digestionEfficiency, random, 1)
            maxAge = mutateValue(maxAge, random, 50)
            perception = mutateValue(perception, random, 1)

            val child = Genome(speed, metabolism, digestionEfficiency, maxAge, perception)
            // Log.d("Genome", "Inherit => mother=$mother father=$father child=$child")
            return child
        }
    }
} 