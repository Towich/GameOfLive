package com.example.gameoflive.domain.model

import com.example.gameoflive.model.Genome
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Информация о сохранении, отображается в списке.
 */
data class SaveInfo(
    val fileName: String,
    val name: String,
    val tickCounter: Long,
    val organismCount: Int,
    val medianGenome: Genome,
    val saveDate: Long
) {
    fun getFormattedDate(): String {
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(saveDate), ZoneId.systemDefault())
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }
} 