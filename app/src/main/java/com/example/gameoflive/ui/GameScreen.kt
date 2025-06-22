package com.example.gameoflive.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.gameoflive.ui.theme.DesignConstants
import kotlin.random.Random
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log

@Composable
fun GameScreen(
    saveFileName: String? = null,
    onBackToMenu: () -> Unit = {}
) {
    // Используем Hilt для ViewModel
    val viewModel: GameViewModel = hiltViewModel()
    
    // Загружаем симуляцию, если указано имя файла
    LaunchedEffect(saveFileName) {
        Log.d("GameScreen", "LaunchedEffect triggered with saveFileName: $saveFileName")
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
        ExitDialog(
            onConfirm = { viewModel.confirmExit() },
            onDismiss = { viewModel.cancelExit() }
        )
    }
    
    // UI
    val currentSimulation = state.simulation
    if (currentSimulation == null) {
        LoadingScreen()
        return
    }
    
    // Основной контейнер с градиентным фоном
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        // Анимированные частицы на фоне
        AnimatedParticles()
        
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignConstants.SCREEN_PADDING)
        ) {
            // Заголовок с статистикой
            GameHeader(currentSimulation, state.medianGenome)
            
            Spacer(modifier = Modifier.height(DesignConstants.ELEMENT_SPACING))
            
            // Сетка поля
            GameGridView(
                simulation = currentSimulation,
                tick = currentSimulation.tickCounter,
                onCellClick = { pos ->
                    viewModel.dispatch(GameIntent.CellClicked(pos))
                }
            )
            
            Spacer(modifier = Modifier.height(DesignConstants.ELEMENT_SPACING))
            
            // Панель управления
            ControlPanel(
                onSpawnOrganisms = { viewModel.dispatch(GameIntent.SpawnOrganisms) },
                onFastForward = { viewModel.dispatch(GameIntent.FastForward) },
                onSave = { viewModel.dispatch(GameIntent.Save("")) },
                onBackToMenu = { viewModel.dispatch(GameIntent.BackPressed) }
            )
            
            Spacer(modifier = Modifier.height(DesignConstants.ELEMENT_SPACING))
            
            // Список организмов
            OrganismsList(currentSimulation.organisms.toList())
        }
        
        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
    
    // Диалог выбранного организма
    state.selectedOrganism?.let { org ->
        OrganismDialog(
            organism = org,
            onDismiss = {
                viewModel.dispatch(GameIntent.ClearSelectedOrganism)
            }
        )
    }
    
    // Диалог сохранения
    if (state.showSaveDialog) {
        SaveDialog(
            onSave = { name ->
                if (name.isNotBlank()) {
                    viewModel.dispatch(GameIntent.Save(name))
                }
            },
            onCancel = { viewModel.dispatch(GameIntent.CancelSave) }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF4ECDC4),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Загрузка симуляции...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GameHeader(simulation: Simulation, medianGenome: com.example.gameoflive.model.Genome?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3E50).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Игра Жизни",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Тик", simulation.tickCounter.toString(), Color(0xFF4ECDC4))
                StatItem("Организмы", simulation.organisms.size.toString(), Color(0xFFFF6B6B))
                StatItem("Еда", simulation.food.size.toString(), Color(0xFF26D0CE))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Информация о медианном геноме
            if (medianGenome != null && simulation.organisms.isNotEmpty()) {
                Text(
                    text = "Медианный геном",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ECDC4),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GenomeStat("Скорость", medianGenome.speed.toString(), Color(0xFF26D0CE))
                    GenomeStat("Метаболизм", medianGenome.metabolism.toString(), Color(0xFFFF6B6B))
                    GenomeStat("Пищеварение", medianGenome.digestionEfficiency.toString(), Color(0xFF4ECDC4))
                    GenomeStat("Восприятие", medianGenome.perception.toString(), Color(0xFF9B59B6))
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GenomeStat("Макс. возраст", medianGenome.maxAge.toString(), Color(0xFF95A5A6))
                }
            }
        }
    }
}

@Composable
private fun GenomeStat(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFFB8B8B8)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFB8B8B8)
        )
    }
}

@Composable
private fun GameGridView(
    simulation: Simulation,
    tick: Long,
    onCellClick: (Position) -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val minCellDp = 4.dp
    val maxCellDp = 20.dp

    // Ширина/высота экрана в dp
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    // Доступная область
    val verticalReservedDp = 300.dp
    val availableWidthDp = screenWidthDp - 48.dp
    val availableHeightDp = screenHeightDp - verticalReservedDp

    // Размер клетки
    val fitWidthCell = availableWidthDp / simulation.width
    val fitHeightCell = availableHeightDp / simulation.height
    var cellSizeDp = minOf(fitWidthCell, fitHeightCell)
    cellSizeDp = cellSizeDp.coerceIn(minCellDp, maxCellDp)

    val cellSizePx = with(density) { cellSizeDp.toPx() }
    val fieldWidthDp = (simulation.width * cellSizeDp.value).dp
    val fieldHeightDp = (simulation.height * cellSizeDp.value).dp

    val needHorizontalScroll = fieldWidthDp > availableWidthDp
    val needVerticalScroll = fieldHeightDp > availableHeightDp

    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3E50).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .then(if (needHorizontalScroll) Modifier.horizontalScroll(hScroll) else Modifier)
                .then(if (needVerticalScroll) Modifier.verticalScroll(vScroll) else Modifier)
                .width(fieldWidthDp.coerceAtMost(availableWidthDp))
                .height(fieldHeightDp.coerceAtMost(availableHeightDp))
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .pointerInput(tick) {
                    detectTapGestures { offset ->
                        val x = (offset.x / cellSizePx).toInt()
                        val y = (offset.y / cellSizePx).toInt()
                        if (x in 0 until simulation.width && y in 0 until simulation.height) {
                            onCellClick(Position(x, y))
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Еда
                simulation.food.forEach { foodPos ->
                    drawRect(
                        color = Color(0xFFEEEEEE),
                        topLeft = Offset(foodPos.x * cellSizePx, foodPos.y * cellSizePx),
                        size = Size(cellSizePx, cellSizePx)
                    )
                }
                // Организмы
                simulation.organisms.forEach { org ->
                    val color = Color(org.genome.color)
                    drawRect(
                        color = color,
                        topLeft = Offset(org.position.x * cellSizePx, org.position.y * cellSizePx),
                        size = Size(cellSizePx, cellSizePx)
                    )
                }
                // Сетка
                val gridColor = Color(0xFFE0E0E0)
                val stroke = 0.5f
                for (x in 0..simulation.width) {
                    val xCoord = x * cellSizePx
                    drawLine(
                        gridColor,
                        Offset(xCoord, 0f),
                        Offset(xCoord, fieldHeightDp.toPx()),
                        strokeWidth = stroke
                    )
                }
                for (y in 0..simulation.height) {
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
}

@Composable
private fun ControlPanel(
    onSpawnOrganisms: () -> Unit,
    onFastForward: () -> Unit,
    onSave: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3E50).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ControlButton(
                icon = Icons.Default.Add,
                label = "Добавить",
                onClick = onSpawnOrganisms,
                color = Color(0xFF4ECDC4)
            )
            
            ControlButton(
                icon = Icons.Default.PlayArrow,
                label = "Ускорить",
                onClick = onFastForward,
                color = Color(0xFFFF6B6B)
            )
            
            ControlButton(
                icon = Icons.Default.Save,
                label = "Сохранить",
                onClick = onSave,
                color = Color(0xFF26D0CE)
            )
            
            ControlButton(
                icon = Icons.Default.Home,
                label = "Меню",
                onClick = onBackToMenu,
                color = Color(0xFF95A5A6)
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = color),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OrganismsList(organisms: List<Organism>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3E50).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(organisms) { org ->
                val sexColor = Color(org.genome.color)
                Text(
                    text = "#${org.id} (${if (org.sex == Sex.MALE) "M" else "F"}) E:${org.energy} A:${org.age}",
                    fontSize = 12.sp,
                    color = sexColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ExitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Выйти из симуляции?",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = { 
            Text(
                "Все несохранённые изменения будут потеряны.",
                color = Color(0xFFB8B8B8)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
            ) { 
                Text("Выйти") 
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
            ) { 
                Text("Отмена") 
            }
        },
        containerColor = Color(0xFF2C3E50),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SaveDialog(
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = { 
            Text(
                "Сохранить игру",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название сохранения") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
            ) { 
                Text("Сохранить") 
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF95A5A6))
            ) { 
                Text("Отмена") 
            }
        },
        containerColor = Color(0xFF2C3E50),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun OrganismDialog(
    organism: Organism,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Организм ${organism.id}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                InfoRow("Позиция", "(${organism.position.x}, ${organism.position.y})")
                InfoRow("Энергия", organism.energy.toString())
                InfoRow("Возраст", organism.age.toString())
                InfoRow("Фаза", organism.phase.toString())
                InfoRow("Пол", if (organism.sex == Sex.MALE) "Мужской" else "Женский")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Геном:",
                    color = Color(0xFF4ECDC4),
                    fontWeight = FontWeight.Bold
                )
                InfoRow("Скорость", organism.genome.speed.toString())
                InfoRow("Метаболизм", organism.genome.metabolism.toString())
                InfoRow("Эфф. пищеварения", organism.genome.digestionEfficiency.toString())
                InfoRow("Восприятие", organism.genome.perception.toString())
                InfoRow("Макс. возраст", organism.genome.maxAge.toString())
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
            ) { 
                Text("OK") 
            }
        },
        containerColor = Color(0xFF2C3E50),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            color = Color(0xFFB8B8B8),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AnimatedParticles() {
    val particles = remember { List(15) { GameParticle() } }
    
    particles.forEach { particle ->
        val infiniteTransition = rememberInfiniteTransition(label = "particle")
        
        val xOffset by infiniteTransition.animateFloat(
            initialValue = particle.initialX,
            targetValue = particle.targetX,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "xOffset"
        )
        
        val yOffset by infiniteTransition.animateFloat(
            initialValue = particle.initialY,
            targetValue = particle.targetY,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "yOffset"
        )
        
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration / 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        
        Box(
            modifier = Modifier
                .offset(x = xOffset.dp, y = yOffset.dp)
                .size(particle.size.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    when (particle.type) {
                        GameParticleType.TEAL -> Color(0xFF4ECDC4).copy(alpha = alpha)
                        GameParticleType.ORANGE -> Color(0xFFFF6B6B).copy(alpha = alpha)
                        GameParticleType.WHITE -> Color.White.copy(alpha = alpha * 0.3f)
                    },
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

private enum class GameParticleType {
    TEAL, ORANGE, WHITE
}

private data class GameParticle(
    val initialX: Float = (0..400).random().toFloat(),
    val targetX: Float = (0..400).random().toFloat(),
    val initialY: Float = (0..800).random().toFloat(),
    val targetY: Float = (0..800).random().toFloat(),
    val duration: Int = (6000..15000).random(),
    val size: Float = 1f + (3f) * Random.nextFloat(),
    val type: GameParticleType = GameParticleType.values()[Random.nextInt(GameParticleType.values().size)]
) 