package com.example.gameoflive.data.starwars

import androidx.compose.runtime.MutableState
import com.example.gameoflive.data.Cell
import com.example.gameoflive.data.CellDirection
import com.example.gameoflive.data.CellType
import com.example.gameoflive.data.CoreLogic
import kotlin.random.Random

class StarWars {

    fun processStarWarsIteration(
        cells: List<List<MutableState<Cell>>>,
        copyCells: MutableList<MutableList<Cell>>,
    ) {
        for (x in cells.indices) {
            for (y in 0..<cells[x].size) {
                val cell = cells[x][y].value
                if (cell.type == CellType.BULLET) {
                    when (cell.direction) {
                        CellDirection.UP -> {
                            if (x - 1 < 0) {
                                cells[x][y].value = cells[x][y].value.copy(type = CellType.JEDI)
                                continue
                            }
                            val local = cells[x][y].value
                            cells[x][y].value = cells[x - 1][y].value
                            cells[x - 1][y].value = local
                        }

                        CellDirection.RIGHT -> {
                            if (y + 1 > cells[x].size - 1) {
                                cells[x][y].value = cells[x][y].value.copy(type = CellType.JEDI)
                                continue
                            }
                            val local = cells[x][y].value
                            cells[x][y].value = cells[x][y + 1].value
                            cells[x][y + 1].value = local
                        }

                        CellDirection.DOWN -> {
                            if (x + 1 > cells.size - 1) {
                                cells[x][y].value = cells[x][y].value.copy(type = CellType.JEDI)
                                continue
                            }
                            val local = cells[x][y].value
                            cells[x][y].value = cells[x + 1][y].value
                            cells[x + 1][y].value = local
                        }

                        CellDirection.LEFT -> {
                            if (y - 1 < 0) {
                                cells[x][y].value = cells[x][y].value.copy(type = CellType.JEDI)
                                continue
                            }
                            val local = cells[x][y].value
                            cells[x][y].value = cells[x][y - 1].value
                            cells[x][y - 1].value = local
                        }

                        CellDirection.NONE -> {
                            cells[x][y].value = cells[x][y].value.copy(type = CellType.JEDI)
                        }
                    }
                } else if (cell.alive == 1) {
                    val neighbors = CoreLogic.getNeighbors(
                        cells = cells,
                        x = x,
                        y = y,
                        onlyFree = false
                    )

                    for (neighbor in neighbors) {
                        val cellNeighbor = cells[neighbor.first][neighbor.second]
                        if (cellNeighbor.value.type == CellType.BULLET
                            && cellNeighbor.value.owner != null
                            && cellNeighbor.value.owner!!.first != x
                            && cellNeighbor.value.owner!!.second != y
                        ) {
                            cells[neighbor.first][neighbor.second].value =
                                cells[neighbor.first][neighbor.second].value.copy(
                                    type = CellType.JEDI,
                                    alive = 0,
                                    owner = null,
                                    direction = CellDirection.NONE
                                )

                            cells[x][y].value =
                                cells[x][y].value.copy(alive = 0)
                            break
                        }
                    }

                    val randomNeighbor = neighbors[Random.nextInt(0, neighbors.size)]
                    if (cells[randomNeighbor.first][randomNeighbor.second].value.alive == 1) {
                        continue
                    }

                    val isShooting = Random.nextInt(0, 100) < SPAWN_BUTTON_CHANCE
                    if (isShooting) {
                        val cellDirection = when {
                            randomNeighbor.first < cell.x -> {
                                CellDirection.UP
                            }

                            randomNeighbor.first == cell.x -> {
                                if(randomNeighbor.second < cell.y) {
                                    CellDirection.LEFT
                                } else {
                                    CellDirection.RIGHT
                                }
                            }

                            else -> {
                                CellDirection.DOWN
                            }
                        }

                        cells[randomNeighbor.first][randomNeighbor.second].value =
                            cells[randomNeighbor.first][randomNeighbor.second].value.copy(
                                type = CellType.BULLET,
                                direction = cellDirection,
                                alive = 0,
                                owner = Pair(x, y)
                            )

                        continue
                    }

                    val isStaying = Random.nextInt(0, 100) < IS_STAYING_CHANCE
                    if (isStaying) {
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

    companion object {
        private const val SPAWN_BUTTON_CHANCE = 5
        private const val IS_STAYING_CHANCE = 70
    }
}