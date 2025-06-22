package com.example.gameoflive.domain.model

import com.example.gameoflive.model.Genome

/**
 * Информация о сохранении, отображается в списке.
 */
data class SaveInfo(
    val fileName: String,
    val name: String,
    val tickCounter: Long,
    val organismCount: Int,
    val medianGenome: Genome
) 