package com.example.gameoflive.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameoflive.data.Cell
import com.example.gameoflive.data.CellType
import com.example.gameoflive.data.GameCore
import com.example.gameoflive.ui.theme.GameOfLiveTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val gameCore: GameCore = GameCore()
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
//                    ConwoysGame(
//                        m = 120,
//                        n = 55,
//                        gameActive = gameActive,
//                        onChangeUpdater = { cells ->
//                            if (updater != null) {
//                                updater!!.interrupt()
//                                updater = null
//                            } else {
//                                updater = gameCore.createGameUpdaterThread(cells)
//                                updater!!.start()
//                            }
//                            gameActive = updater != null
//                        }
//                    )
                    StarWarsGame(m = 120, n = 55, gameActive = gameActive) { cells ->
                        if (updater != null) {
                            updater!!.interrupt()
                            updater = null
                        } else {
                            updater = gameCore.createGameUpdaterThread(cells)
                            updater!!.start()
                        }
                        gameActive = updater != null
                    }
                }
            }
        }
    }
}

@Composable
fun ConwoysGame(
    m: Int,
    n: Int,
    gameActive: Boolean,
    onChangeUpdater: (cells: List<List<MutableState<Cell>>>) -> Unit
) {
    val cellState = remember {
        List(m) {
            List(n) {
                mutableStateOf(
                    Cell(
                        x = 0,
                        y = 0,
                        type = if (Random.nextInt(1, 100) < 50) CellType.JEDI else CellType.DROID,
                        alive = if (Random.nextInt(1, 100) < 80) 0 else 1,
                        neighbors = 0,
                        aliveTimes = 0
                    )
                )
            }
        }
    }

    var x = 0
    for (row in cellState) {
        var y = 0
        for (cell in row) {
            cell.value = cell.value.copy(x = x, y = y)
            y++
        }
        x++
    }

    Column {
        for (list in cellState) {
            Row {
                for (cell in list) {
                    ConwoysCellComposable(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onChangeUpdater(cellState) },
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .width(82.dp)
                    .height(41.dp)
            ) {
                Text(text = if (!gameActive) "START" else "STOP", fontSize = 10.sp)
            }
//            Text(text = "Droids: $droidsAlive")
        }
    }
}

@Composable
fun StarWarsGame(
    m: Int,
    n: Int,
    gameActive: Boolean,
    onChangeUpdater: (cells: List<List<MutableState<Cell>>>) -> Unit
) {
    val cellState = remember {
        List(m) {
            List(n) {
                mutableStateOf(
                    Cell(
                        x = 0,
                        y = 0,
                        type = if (Random.nextInt(1, 100) < 50) CellType.JEDI else CellType.DROID,
                        alive = if (Random.nextInt(1, 100) < 80) 0 else 1,
                        neighbors = 0,
                        aliveTimes = 0
                    )
                )
            }
        }
    }

    var jedisAlive = 0
    var droidsAlive = 0

    var x = 0
    for (row in cellState) {
        var y = 0
        for (cell in row) {
            cell.value = cell.value.copy(x = x, y = y)
            if (cell.value.alive == 1) {
                when (cell.value.type) {
                    CellType.JEDI -> jedisAlive++
                    CellType.DROID -> droidsAlive++
                    CellType.BULLET -> {}
                }
            }
            y++
        }
        x++
    }

    Column {
        for (list in cellState) {
            Row {
                for (cell in list) {
                    StarWarsCellComposable(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Jedis: $jedisAlive")
            Button(
                onClick = { onChangeUpdater(cellState) },
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .width(82.dp)
                    .height(41.dp)
            ) {
                Text(text = if (!gameActive) "START" else "STOP", fontSize = 10.sp)
            }
            Text(text = "Droids: $droidsAlive")
        }
    }
}

