package com.example.gameoflive

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameoflive.ui.theme.GameOfLiveTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private var updater: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var gameActive by remember {
                mutableStateOf(updater != null)
            }
            GameOfLiveTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Game(
                        gameActive = gameActive,
                        onChangeUpdater = { thread ->
                            if (updater != null) {
                                updater!!.interrupt()
                                updater = null
                            } else {
                                updater = thread
                                updater!!.start()
                            }
                            gameActive = updater != null
                        }
                    )
                }
            }
        }
    }
}

data class Cell(
    val x: Int,
    val y: Int,
    val alive: Int,
    val neighbors: Int,
    val aliveTimes: Int
)

@Composable
fun Game(gameActive: Boolean, onChangeUpdater: (thread: Thread) -> Unit) {
    val m = 120
    val n = 55

    val cellState = remember {
        List(m) {
            List(n) { mutableStateOf(Cell(0, 0, if (Random.nextInt(1, 100) < 95) 0 else 1, 0, 0)) }
        }
    }

    Column {
        for (list in cellState) {
            Row {
                for (cell in list) {
                    CellComposable(
                        cell = cell.value,
                        onClick = {
                            cell.value =
                                cell.value.copy(alive = if (cell.value.alive == 0) 1 else 0)
                        })
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Button(
            onClick = { onChangeUpdater(gameUpdater(cells = cellState)) },
            modifier = Modifier
                .padding(bottom = 20.dp)
                .width(82.dp)
                .height(41.dp)
        ) {
            Text(text = if (!gameActive) "START" else "STOP", fontSize = 10.sp)
        }
    }
}

@Composable
fun CellComposable(cell: Cell, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(if (cell.aliveTimes > 5) Color.Green else if (cell.alive == 1) Color.Black else Color.White)
            .border(width = 0.1.dp, color = Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
//        Text(text = cell.neighbors.toString(), color = Color.Blue)
    }
}

fun gameUpdater(
    cells: List<List<MutableState<Cell>>>
): Thread {
    val runnable = Runnable {
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
                var aliveNeighbors = 0

                for (nx in -1..1) {
                    for (ny in -1..1) {
                        if (nx == 0 && ny == 0) {
                            continue
                        }

                        if (nx + x < 0 || nx + x > copyCells.size - 1
                            || ny + y < 0 || ny + y > copyCells[y].size - 1
                        ) {
                            continue
                        }

                        if (copyCells[nx + x][ny + y].alive == 1) {
                            aliveNeighbors++
                        }
                    }
                }

                if (x == 2 && y == 1) {
                    println("aliveNeighbors=$aliveNeighbors")
                }

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