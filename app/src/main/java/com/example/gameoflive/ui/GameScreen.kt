package com.example.gameoflive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameoflive.GameConfig
import com.example.gameoflive.model.Organism
import com.example.gameoflive.model.Position
import com.example.gameoflive.model.Sex
import com.example.gameoflive.model.Simulation
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen() {
    val simulation =
        remember { Simulation(width = GameConfig.FIELD_WIDTH, height = GameConfig.FIELD_HEIGHT) }

    val tickTrigger = remember { mutableStateOf(0L) }
    val selectedOrganism: MutableState<Organism?> = remember { mutableStateOf(null) }

    // создаём первый организм, если его нет
    if (simulation.organisms.isEmpty()) {
        simulation.spawnOrganism(
            genome = GameConfig.randomGenome(),
            position = Position(simulation.width / 2, simulation.height / 2),
            random = Random
        )
    }

    // Главный цикл симуляции
    LaunchedEffect(Unit) {
        while (true) {
            delay(300L)
            simulation.tick()
            tickTrigger.value = simulation.tickCounter // заставляем UI перерисоваться
        }
    }

    Column(Modifier.padding(16.dp)) {
        ControlPanel(
            onAddOrganism = {
                val pos =
                    Position(Random.nextInt(simulation.width), Random.nextInt(simulation.height))
                if (simulation.isCellFree(pos)) {
                    simulation.spawnOrganism(GameConfig.randomGenome(), pos)
                }
            }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        val tick = tickTrigger.value // подписка на изменения

        // статистика
        Text("Тик: $tick  Организмов: ${simulation.organisms.size}  Еды: ${simulation.food.size}")

        Spacer(Modifier.padding(4.dp))

        // Сетка поля
        GridView(simulation, tick, onCellClick = { pos ->
            val org =
                simulation.organisms.firstOrNull { it.position.x == pos.x && it.position.y == pos.y }
            selectedOrganism.value = org
        })

        Spacer(Modifier.padding(4.dp))

        // Мониторинг организмов (расширенный)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color.Gray)
                .padding(4.dp)
        ) {
            items(simulation.organisms.toList()) { org ->
                val sexColor = if (org.sex == Sex.MALE) Color(0xFF2196F3) else Color(0xFFFF69B4)
                Text(
                    text = "#${org.id} (${if (org.sex == Sex.MALE) "M" else "F"}) E:${org.energy} " +
                            "A:${org.age} G:[S:${org.genome.speed} M:${org.genome.metabolism} D:${org.genome.digestionEfficiency} Max:${org.genome.maxAge}]",
                    fontSize = 11.sp,
                    color = sexColor
                )
            }
        }
    }

    selectedOrganism.value?.let { org ->
        OrganismDialog(organism = org, onDismiss = { selectedOrganism.value = null })
    }
}

@Composable
private fun ControlPanel(onAddOrganism: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onAddOrganism) { Text("Добавить организм") }
    }
}

@Composable
private fun GridView(sim: Simulation, tick: Long, onCellClick: (Position) -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val minCellDp = 4.dp
    val maxCellDp = 24.dp

    // Ширина/высота экрана в dp
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    // Доступная область (учитываем, что сверху есть панель управления и статистика)
    val verticalReservedDp = 240.dp // примерно высота ControlPanel + списки
    val availableWidthDp = screenWidthDp - 32.dp  // учтём отступы
    val availableHeightDp = screenHeightDp - verticalReservedDp

    // Кандидат размера клетки так, чтобы всё поместилось без скролла
    val fitWidthCell = availableWidthDp / sim.width
    val fitHeightCell = availableHeightDp / sim.height
    var cellSizeDp = minOf(fitWidthCell, fitHeightCell)

    // Ограничиваем допустимым диапазоном
    cellSizeDp = cellSizeDp.coerceIn(minCellDp, maxCellDp)

    val cellSizePx = with(density) { cellSizeDp.toPx() }

    val fieldWidthDp = (sim.width * cellSizeDp.value).dp
    val fieldHeightDp = (sim.height * cellSizeDp.value).dp

    val needHorizontalScroll = fieldWidthDp > availableWidthDp
    val needVerticalScroll = fieldHeightDp > availableHeightDp

    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .then(if (needHorizontalScroll) Modifier.horizontalScroll(hScroll) else Modifier)
            .then(if (needVerticalScroll) Modifier.verticalScroll(vScroll) else Modifier)
            .width(fieldWidthDp.coerceAtMost(availableWidthDp))
            .height(fieldHeightDp.coerceAtMost(availableHeightDp))
            .border(1.dp, Color.Gray)
            .pointerInput(tick) {
                detectTapGestures { offset ->
                    val x = (offset.x / cellSizePx).toInt()
                    val y = (offset.y / cellSizePx).toInt()
                    if (x in 0 until sim.width && y in 0 until sim.height) {
                        onCellClick(Position(x, y))
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Белый фон
            drawRect(Color.White)
            // Еда
            sim.food.forEach { foodPos ->
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(foodPos.x * cellSizePx, foodPos.y * cellSizePx),
                    size = Size(cellSizePx, cellSizePx)
                )
            }
            // Организмы
            sim.organisms.forEach { org ->
                val color = if (org.sex == Sex.MALE) Color(0xFF2196F3) else Color(0xFFFF69B4)
                drawRect(
                    color = color,
                    topLeft = Offset(org.position.x * cellSizePx, org.position.y * cellSizePx),
                    size = Size(cellSizePx, cellSizePx)
                )
            }
            // Сетка
            val gridColor = Color.LightGray
            val stroke = 0.5f
            for (x in 0..sim.width) {
                val xCoord = x * cellSizePx
                drawLine(gridColor, Offset(xCoord, 0f), Offset(xCoord, fieldHeightDp.toPx()), strokeWidth = stroke)
            }
            for (y in 0..sim.height) {
                val yCoord = y * cellSizePx
                drawLine(gridColor, Offset(0f, yCoord), Offset(fieldWidthDp.toPx(), yCoord), strokeWidth = stroke)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrganismDialog(organism: Organism, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        },
        title = { Text("Организм ${organism.id}") },
        text = {
            Column {
                Text("Позиция: (${organism.position.x}, ${organism.position.y})")
                Text("Энергия: ${organism.energy}")
                Text("Возраст: ${organism.age}")
                Text("Фаза: ${organism.phase}")
                Text("Секс: ${organism.sex}")
                Spacer(Modifier.padding(4.dp))
                Text("Геном:")
                Text("  Скорость: ${organism.genome.speed}")
                Text("  Метаболизм: ${organism.genome.metabolism}")
                Text("  Эфф. пищеварения: ${organism.genome.digestionEfficiency}")
                Text("  Макс. возраст: ${organism.genome.maxAge}")
            }
        }
    )
} 