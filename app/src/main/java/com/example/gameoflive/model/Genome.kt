package com.example.gameoflive.model

import android.util.Log
import kotlin.random.Random
import com.example.gameoflive.GameConfig

/**
 * Простая модель генома, который отвечает за основные параметры организма.
 * Все параметры выражены в целых числах для простоты вычислений.
 */
data class Genome(
    val speed: Int,               // количество клеток, которое организм может пройти за один тик
    val metabolism: Int,          // энергия, затрачиваемая каждый тик на поддержание жизнедеятельности
    val digestionEfficiency: Int, // сколько энергии организм получает из одной еды
    val maxAge: Int               // максимальный возраст организма в тиках
) {

    companion object {
        fun random(random: Random = Random): Genome {
            val genome = GameConfig.randomGenome(random)
            Log.d("Genome", "Random genome generated: $genome")
            return genome
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

            // небольшая мутация с некоторой вероятностью
            if (random.nextInt(100) < GameConfig.MUTATION_CHANCE) speed =
                (speed + random.nextInt(-GameConfig.MUTATION_DELTA, GameConfig.MUTATION_DELTA + 1)).coerceAtLeast(1)
            if (random.nextInt(100) < GameConfig.MUTATION_CHANCE) metabolism =
                (metabolism + random.nextInt(-GameConfig.MUTATION_DELTA, GameConfig.MUTATION_DELTA + 1)).coerceAtLeast(1)
            if (random.nextInt(100) < GameConfig.MUTATION_CHANCE) digestionEfficiency =
                (digestionEfficiency + random.nextInt(
                    -GameConfig.MUTATION_DELTA,
                    GameConfig.MUTATION_DELTA + 1
                )).coerceAtLeast(1)
            if (random.nextInt(100) < GameConfig.MUTATION_CHANCE) maxAge =
                (maxAge + random.nextInt(-10, 11)).coerceAtLeast(50)

            val child = Genome(speed, metabolism, digestionEfficiency, maxAge)
            Log.d("Genome", "Inherit => mother=$mother father=$father child=$child")
            return child
        }
    }
} 