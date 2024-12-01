package com.example.gameoflive

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState

class GameCore {

    fun createGameUpdaterThread(
        cells: List<List<MutableState<Cell>>>
    ): Thread {

        val runnable = createGameUpdateRunnable(cells = cells)
        val t = object : Thread() {
            override fun run() {
                while (!isInterrupted) {
                    try {
                        sleep(250)
                        val handler = Handler(Looper.getMainLooper())
                        handler.post(runnable)
                        println("SUP")

                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        }
        return t
    }

    private fun createGameUpdateRunnable(cells: List<List<MutableState<Cell>>>): Runnable {
        return Runnable {
            val copyCells = mutableListOf<MutableList<Cell>>()

            for (row in cells) {
                val newRow = mutableListOf<Cell>()
                for (stateCell in row) {
                    newRow.add(stateCell.value)
                }
                copyCells.add(newRow)
            }

            for (x in copyCells.indices) {
                for (y in 0..<copyCells[x].size) {
                    val aliveNeighbors = countAliveNeighbors(cells = copyCells, x = x, y = y)

                    if (copyCells[x][y].alive == 1) {
                        if (aliveNeighbors != 2 && aliveNeighbors != 3) {
                            cells[x][y].value = copyCells[x][y].copy(alive = 0, aliveTimes = 0)
                        } else {
                            cells[x][y].value = copyCells[x][y].copy(aliveTimes = cells[x][y].value.aliveTimes + 1)
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

    private fun countAliveNeighbors(cells: MutableList<MutableList<Cell>>, x: Int, y: Int): Int {
        var aliveNeighbors = 0

        for (nx in -1..1) {
            for (ny in -1..1) {
                if (nx == 0 && ny == 0) {
                    continue
                }

                if (nx + x < 0 || nx + x > cells.size - 1
                    || ny + y < 0 || ny + y > cells[y].size - 1
                ) {
                    continue
                }

                if (cells[nx + x][ny + y].alive == 1) {
                    aliveNeighbors++
                }
            }
        }

        return aliveNeighbors
    }
}