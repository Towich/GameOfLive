package com.example.gameoflive.data

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import com.example.gameoflive.data.conwoy.GameOfLiveConwoy
import com.example.gameoflive.data.starwars.StarWars

class GameCore {

    private val gameOfLiveConwoy: GameOfLiveConwoy = GameOfLiveConwoy()
    private val starWars: StarWars = StarWars()

    fun createGameUpdaterThread(
        cells: List<List<MutableState<Cell>>>
    ): Thread {
        val runnable = createGameUpdateRunnable(cells = cells)
        val t = object : Thread() {
            override fun run() {
                while (!isInterrupted) {
                    try {
                        sleep(UPDATE_MILLISECONDS)
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

//            gameOfLiveConwoy.processGameOfLiveIteration(cells = cells, copyCells = copyCells)
            starWars.processStarWarsIteration(cells = cells, copyCells = copyCells)
        }
    }

    companion object {
        private const val UPDATE_MILLISECONDS = 250L
    }
}