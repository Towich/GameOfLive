package com.example.gameoflive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CellComposable(cell: Cell, onClick: () -> Unit) {
    val cellBackground = if(cell.alive == 1) {
        when (cell.type) {
            CellType.JEDI -> {
                Color.Blue
            }

            CellType.DROID -> {
                Color.Gray
            }
        }
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .size(16.dp)
            .background(cellBackground)
            .border(width = 0.1.dp, color = Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
//        Text(text = cell.neighbors.toString(), color = Color.Blue)
    }
}