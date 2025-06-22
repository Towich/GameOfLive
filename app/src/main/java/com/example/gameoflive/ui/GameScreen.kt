package com.example.gameoflive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gameoflive.GameConfig
import com.example.gameoflive.model.Organism
import com.example.gameoflive.model.Position
import com.example.gameoflive.model.Sex
import com.example.gameoflive.model.Simulation
import com.example.gameoflive.presentation.game.GameEffect
import com.example.gameoflive.presentation.game.GameIntent
import com.example.gameoflive.presentation.game.GameState
import com.example.gameoflive.presentation.game.GameViewModel
import androidx.activity.compose.BackHandler
import kotlin.random.Random
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GameScreen(
    saveFileName: String? = null,
    onBackToMenu: () -> Unit = {}
) {
    // Используем Hilt для ViewModel
    val viewModel: GameViewModel = hiltViewModel()
    
    // Загружаем симуляцию, если указано имя файла
    LaunchedEffect(saveFileName) {
        if (saveFileName != null) {
            viewModel.loadSimulation(saveFileName)
        } else {
            // Создаём новую симуляцию
            val newSimulation = Simulation(
                width = GameConfig.FIELD_WIDTH,
                height = GameConfig.FIELD_HEIGHT
            ).apply {
                if (organisms.isEmpty()) {
                    spawnOrganism(
                        genome = GameConfig.randomGenome(),
                        position = Position(width / 2, height / 2),
                        random = Random
                    )
                }
            }
            viewModel.initialize(newSimulation)
        }
    }
    
    // Подписываемся на состояние и эффекты
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Обрабатываем эффекты
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GameEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is GameEffect.NavigateBack -> onBackToMenu()
            }
        }
    }
    
    // Обрабатываем системную кнопку "Назад"
    BackHandler(enabled = true) {
        viewModel.dispatch(GameIntent.BackPressed)
    }
    
    // Показываем диалог выхода, если нужно
    if (state.showExitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelExit() },
            title = { Text("Выйти из симуляции?") },
            text = { Text("Все несохранённые изменения будут потеряны.") },
            confirmButton = {
                Button(onClick = { viewModel.confirmExit() }) { Text("Выйти") }
            },
            dismissButton = {
                Button(onClick = { viewModel.cancelExit() }) { Text("Отмена") }
            }
        )
    }
    
    // UI
    val currentSimulation = state.simulation
    if (currentSimulation == null) {
        Text("Загрузка...")
        return
    }
    
    // Контейнер для контента и FAB'ов
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // статистика
            Text("Тик: ${currentSimulation.tickCounter}  Организмов: ${currentSimulation.organisms.size}  Еды: ${currentSimulation.food.size}")
            
            Spacer(Modifier.padding(4.dp))
            
            // Сетка поля
            GridView(currentSimulation, currentSimulation.tickCounter, onCellClick = { pos ->
                viewModel.dispatch(GameIntent.CellClicked(pos))
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
                items(currentSimulation.organisms.toList()) { org ->
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
        
        // ------ FAB панель ------
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(onClick = {
                viewModel.dispatch(GameIntent.SpawnOrganisms)
            }) { Icon(Icons.Default.Add, contentDescription = "Add Organism") }
            
            SmallFloatingActionButton(onClick = {
                viewModel.dispatch(GameIntent.FastForward)
            }) { Icon(Icons.Default.PlayArrow, contentDescription = "Fast Forward") }
            
            SmallFloatingActionButton(onClick = {
                viewModel.dispatch(GameIntent.Save(""))
            }) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
            
            SmallFloatingActionButton(onClick = {
                viewModel.dispatch(GameIntent.BackPressed)
            }) {
                Icon(Icons.Default.Home, contentDescription = "Menu")
            }
        }
        
        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp) // чуть выше нижних кнопок
        )
    }
    
    // Диалог выбранного организма
    state.selectedOrganism?.let { org ->
        OrganismDialog(organism = org, onDismiss = {
            viewModel.dispatch(GameIntent.ClearSelectedOrganism)
        })
    }
    
    // Диалог сохранения
    if (state.showSaveDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.dispatch(GameIntent.CancelSave) },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.dispatch(GameIntent.Save(name))
                        }
                    }
                ) { Text("Сохранить") }
            },
            dismissButton = {
                Button(onClick = { viewModel.dispatch(GameIntent.CancelSave) }) {
                    Text("Отмена")
                }
            },
            title = { Text("Сохранить игру") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название сохранения") }
                )
            }
        )
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
                drawLine(
                    gridColor,
                    Offset(xCoord, 0f),
                    Offset(xCoord, fieldHeightDp.toPx()),
                    strokeWidth = stroke
                )
            }
            for (y in 0..sim.height) {
                val yCoord = y * cellSizePx
                drawLine(
                    gridColor,
                    Offset(0f, yCoord),
                    Offset(fieldWidthDp.toPx(), yCoord),
                    strokeWidth = stroke
                )
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