package com.example.gameoflive.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import com.example.gameoflive.presentation.game.LoadSimulationViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.ui.theme.DesignConstants
import com.example.gameoflive.ui.components.AnimatedParticles
import com.example.gameoflive.ui.components.GradientBackground
import com.example.gameoflive.ui.components.AnimatedButton
import com.example.gameoflive.ui.components.SaveInfoBottomSheet
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun LoadSimulationScreen(
    onBack: () -> Unit,
    onSelect: (String) -> Unit
) {
    val viewModel: LoadSimulationViewModel = hiltViewModel()
    val saves by viewModel.saves.collectAsState()
    var confirmDelete by remember { mutableStateOf<SaveInfo?>(null) }
    var showContent by remember { mutableStateOf(false) }
    var selectedSaveInfo by remember { mutableStateOf<SaveInfo?>(null) }

    // Загружаем список при первом запуске
    LaunchedEffect(Unit) {
        viewModel.loadSaves()
        delay(DesignConstants.ANIMATION_DELAY.toLong())
        showContent = true
    }

    GradientBackground {
        AnimatedParticles()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(DesignConstants.SCREEN_PADDING)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Заголовок с анимацией
                AnimatedVisibility(
                    visible = showContent,
                    enter = slideInVertically(
                        initialOffsetY = { -100 },
                        animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM, easing = EaseOutBack)
                    ) + fadeIn(animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(DesignConstants.ICON_SIZE),
                            tint = Color(0xFF4ECDC4)
                        )
                        Spacer(modifier = Modifier.height(DesignConstants.ELEMENT_SPACING))
                        Text(
                            text = "Сохранённые симуляции",
                            fontSize = DesignConstants.TITLE_FONT_SIZE,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Выберите сохранение для загрузки",
                            fontSize = DesignConstants.SUBTITLE_FONT_SIZE,
                            color = Color(0xFFB8B8B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = DesignConstants.SMALL_SPACING)
                        )
                    }
                }
                // Список сохранений с анимацией
                AnimatedVisibility(
                    visible = showContent,
                    enter = slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM, delayMillis = DesignConstants.ANIMATION_DELAY, easing = EaseOutBack)
                    ) + fadeIn(animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM, delayMillis = DesignConstants.ANIMATION_DELAY))
                ) {
                    Box(modifier = Modifier.weight(1f, fill = true)) {
                        if (saves.isEmpty()) {
                            EmptyState()
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(DesignConstants.ELEMENT_SPACING),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(saves) { info ->
                                    SaveCard(
                                        saveInfo = info,
                                        onSelect = { onSelect(info.fileName) },
                                        onDelete = { confirmDelete = info },
                                        onInfo = { selectedSaveInfo = info }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            // Кнопка "Назад" всегда внизу
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM, delayMillis = DesignConstants.ANIMATION_DELAY * 2, easing = EaseOutBack)
                ) + fadeIn(animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM, delayMillis = DesignConstants.ANIMATION_DELAY * 2)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                AnimatedButton(
                    text = "Назад",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack,
                    isPrimary = false
                )
            }
            // Диалог подтверждения удаления
            confirmDelete?.let { toDel ->
                DeleteConfirmationDialog(
                    saveInfo = toDel,
                    onConfirm = {
                        viewModel.deleteSave(toDel.fileName) { success ->
                            if (success) confirmDelete = null
                        }
                    },
                    onDismiss = { confirmDelete = null }
                )
            }
        }
        
        // BottomSheet с подробной информацией
        selectedSaveInfo?.let { saveInfo ->
            SaveInfoBottomSheet(
                saveInfo = saveInfo,
                onDismiss = { selectedSaveInfo = null }
            )
        }
    }
}

@Composable
private fun SaveCard(
    saveInfo: SaveInfo,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onInfo: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = tween(DesignConstants.ANIMATION_DURATION_SHORT, easing = EaseOutBack),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 4.dp
            isHovered -> 12.dp
            else -> 8.dp
        },
        animationSpec = tween(DesignConstants.ANIMATION_DURATION_SHORT),
        label = "elevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onSelect() },
        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3E50).copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignConstants.ELEMENT_SPACING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка сохранения
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = Color(0xFF4ECDC4),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(DesignConstants.ELEMENT_SPACING))
            
            // Информация о сохранении
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = saveInfo.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "T: ${saveInfo.tickCounter}",
                        fontSize = 14.sp,
                        color = Color(0xFF95A5A6),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "O: ${saveInfo.organismCount}",
                        fontSize = 14.sp,
                        color = Color(0xFF95A5A6),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = saveInfo.getFormattedDate(),
                    fontSize = 12.sp,
                    color = Color(0xFF7F8C8D),
                    maxLines = 1
                )
            }
            
            // Кнопки действий
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка информации
                IconButton(
                    onClick = onInfo,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF4ECDC4).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Информация",
                        tint = Color(0xFF4ECDC4),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Кнопка удаления
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFE74C3C).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4ECDC4).copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(DesignConstants.ELEMENT_SPACING))
        
        Text(
            text = "Нет сохранений",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(DesignConstants.SMALL_SPACING))
        
        Text(
            text = "Создайте новую симуляцию и сохраните её",
            fontSize = 16.sp,
            color = Color(0xFFB8B8B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    saveInfo: SaveInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Удалить сохранение?",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Вы уверены, что хотите удалить \"${saveInfo.name}\"? Это действие нельзя отменить.",
                color = Color(0xFFB8B8B8)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF95A5A6))
            ) {
                Text("Отмена")
            }
        },
        containerColor = Color(0xFF2C3E50),
        shape = RoundedCornerShape(16.dp)
    )
} 