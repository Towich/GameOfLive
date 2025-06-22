package com.example.gameoflive.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(onNewSimulation: () -> Unit, onLoadSimulation: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Evolution simulation", fontSize = 28.sp)
        Button(onClick = onNewSimulation) { Text("Новая симуляция") }
        Button(onClick = onLoadSimulation) { Text("Загрузить симуляцию") }
    }
} 