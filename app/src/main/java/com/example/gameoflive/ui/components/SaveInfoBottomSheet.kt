package com.example.gameoflive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.model.Genome
import com.example.gameoflive.ui.theme.DesignConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveInfoBottomSheet(
    saveInfo: SaveInfo,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C3E50),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color(0xFF95A5A6)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignConstants.SCREEN_PADDING)
                .verticalScroll(rememberScrollState())
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF4ECDC4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Информация о сохранении",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color(0xFF95A5A6)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Основная информация
            InfoCard(
                title = "Основная информация",
                content = {
                    InfoRow("Название", saveInfo.name)
                    InfoRow("Тик", saveInfo.tickCounter.toString())
                    InfoRow("Организмов", saveInfo.organismCount.toString())
                    InfoRow("Дата сохранения", saveInfo.getFormattedDate())
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Медианный геном
            GenomeCard(
                title = "Медианный геном",
                genome = saveInfo.medianGenome,
                color = Color(0xFF4ECDC4)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Минимальный геном
            GenomeCard(
                title = "Минимальный геном",
                genome = saveInfo.minGenome,
                color = Color(0xFFE74C3C)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Максимальный геном
            GenomeCard(
                title = "Максимальный геном",
                genome = saveInfo.maxGenome,
                color = Color(0xFF2ECC71)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF34495E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFB8B8B8)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun GenomeCard(
    title: String,
    genome: Genome,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF34495E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            GenomeStat("Скорость", genome.speed, color)
            GenomeStat("Метаболизм", genome.metabolism, color)
            GenomeStat("Пищеварение", genome.digestionEfficiency, color)
            GenomeStat("Восприятие", genome.perception, color)
            GenomeStat("Макс. возраст", genome.maxAge, color)
        }
    }
}

@Composable
private fun GenomeStat(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFB8B8B8)
        )
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
} 