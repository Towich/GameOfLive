package com.example.gameoflive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.gameoflive.save.SaveManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

@Composable
fun LoadSimulationScreen(onBack: () -> Unit, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val saves = remember { mutableStateListOf<SaveManager.SaveInfo>().apply { addAll(SaveManager.listSaves(context)) } }
    var confirmDelete by remember { mutableStateOf<SaveManager.SaveInfo?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(saves) { info ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(info.fileName) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = info.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Тик: ${info.tickCounter}    Организмов: ${info.organismCount}")
                            val g = info.medianGenome
                            Text("Median Genome → S:${g.speed} M:${g.metabolism} D:${g.digestionEfficiency} Max:${g.maxAge}")
                        }
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            modifier = Modifier.clickable { confirmDelete = info }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
        }

        // Диалог подтверждения удаления
        confirmDelete?.let { toDel ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Удалить сохранение?") },
                text = { Text("Вы уверены, что хотите удалить \"${toDel.name}\"?") },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        SaveManager.deleteSave(context, toDel.fileName)
                        saves.remove(toDel)
                        confirmDelete = null
                    }) { Text("Удалить") }
                },
                dismissButton = {
                    androidx.compose.material3.Button(onClick = { confirmDelete = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
} 