package com.example.gameoflive

import androidx.compose.runtime.MutableState
import kotlin.random.Random

class StarWars {

    fun processStarWarsIteration(
        cells: List<List<MutableState<Cell>>>,
        copyCells: MutableList<MutableList<Cell>>,
    ) {
        for (x in cells.indices) {
            for (y in 0..<cells[x].size) {
                val cell = cells[x][y].value
                if (cell.alive == 1) {
                    val neighbors = CoreLogic.getNeighbors(
                        cells = copyCells,
                        x = x,
                        y = y,
                        onlyFree = false
                    )

                    for(neighbor in neighbors) {
                        if(cell.type != cells[neighbor.first][neighbor.second].value.type) {
                            val successKill = Random.nextInt(0, 100) < 50
                            if(successKill) {
                                cells[neighbor.first][neighbor.second].value =
                                    cells[neighbor.first][neighbor.second].value.copy(alive = 0)
                                break
                            }
                        }
                    }

                    val isStaying = Random.nextInt(0, 100) < 70
                    if (isStaying) continue

                    val randomNeighbor = neighbors[Random.nextInt(0, neighbors.size)]
                    if(cells[randomNeighbor.first][randomNeighbor.second].value.alive == 1) {
                        continue
                    }

                    val localCell = cells[randomNeighbor.first][randomNeighbor.second].value
                    cells[randomNeighbor.first][randomNeighbor.second].value = cells[x][y].value
                    cells[x][y].value = localCell
                } else if (copyCells[x][y].alive == 0) {

                }
            }
        }
    }
}