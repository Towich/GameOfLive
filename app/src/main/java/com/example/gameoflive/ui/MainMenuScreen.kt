package com.example.gameoflive.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Science
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
import com.example.gameoflive.ui.theme.DesignConstants
import com.example.gameoflive.ui.components.AnimatedParticles
import com.example.gameoflive.ui.components.GradientBackground
import com.example.gameoflive.ui.components.AnimatedButton
import kotlinx.coroutines.delay

@Composable
fun MainMenuScreen(onNewSimulation: () -> Unit, onLoadSimulation: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(DesignConstants.ANIMATION_DELAY.toLong())
        showContent = true
    }
    
    GradientBackground {
        // Анимированные частицы на фоне
        AnimatedParticles()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignConstants.SCREEN_PADDING),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
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
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    AnimatedIcon()
                    
                    Spacer(modifier = Modifier.height(DesignConstants.ELEMENT_SPACING))
                    
                    Text(
                        text = "EvoSim",
                        fontSize = DesignConstants.TITLE_FONT_SIZE,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Эволюционная симуляция",
                        fontSize = DesignConstants.SUBTITLE_FONT_SIZE,
                        color = Color(0xFFB8B8B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = DesignConstants.SMALL_SPACING)
                    )
                }
            }
            
            // Кнопки с анимацией
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM, delayMillis = DesignConstants.ANIMATION_DELAY, easing = EaseOutBack)
                ) + fadeIn(animationSpec = tween(DesignConstants.ANIMATION_DURATION_MEDIUM, delayMillis = DesignConstants.ANIMATION_DELAY))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignConstants.BUTTON_SPACING)
                ) {
                    AnimatedButton(
                        text = "Новая симуляция",
                        icon = Icons.Default.PlayArrow,
                        onClick = onNewSimulation,
                        isPrimary = true
                    )
                    
                    AnimatedButton(
                        text = "Загрузить",
                        icon = Icons.Default.FolderOpen,
                        onClick = onLoadSimulation,
                        isPrimary = false
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "icon")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Icon(
        imageVector = Icons.Default.Science,
        contentDescription = null,
        modifier = Modifier
            .size(DesignConstants.ICON_SIZE)
            .scale(scale),
        tint = Color(0xFF4ECDC4)
    )
} 