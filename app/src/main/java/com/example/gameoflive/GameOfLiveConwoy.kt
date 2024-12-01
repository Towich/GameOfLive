package com.example.gameoflive

import androidx.compose.runtime.MutableState

class GameOfLiveConwoy {

    fun processGameOfLiveIteration(
        cells: List<List<MutableState<Cell>>>,
        copyCells: MutableList<MutableList<Cell>>,
    ) {
        for (x in copyCells.indices) {
            for (y in 0..<copyCells[x].size) {
                val aliveNeighbors = CoreLogic.countAliveNeighbors(cells = copyCells, x = x, y = y)

                if (copyCells[x][y].alive == 1) {
                    if (aliveNeighbors != 2 && aliveNeighbors != 3) {
                        cells[x][y].value = copyCells[x][y].copy(alive = 0, aliveTimes = 0)
                    } else {
                        cells[x][y].value =
                            copyCells[x][y].copy(aliveTimes = cells[x][y].value.aliveTimes + 1)
                    }
                } else if (copyCells[x][y].alive == 0) {
                    if (aliveNeighbors == 3) {
                        cells[x][y].value = copyCells[x][y].copy(alive = 1, aliveTimes = 1)
                    }
                }

                cells[x][y].value = cells[x][y].value.copy(neighbors = aliveNeighbors)
            }
        }
    }
}