package com.example.gameoflive.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameoflive.ui.theme.DesignConstants
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun MainMenuScreen(onNewSimulation: () -> Unit, onLoadSimulation: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(DesignConstants.ANIMATION_DELAY.toLong())
        showContent = true
    }
    
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
    ) {
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
                    MenuButton(
                        text = "Новая симуляция",
                        icon = Icons.Default.PlayArrow,
                        onClick = onNewSimulation,
                        isPrimary = true
                    )
                    
                    MenuButton(
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

@Composable
private fun MenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.05f
            else -> 1f
        },
        animationSpec = tween(DesignConstants.ANIMATION_DURATION_SHORT, easing = EaseOutBack),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 2.dp
            isHovered -> 12.dp
            else -> 8.dp
        },
        animationSpec = tween(DesignConstants.ANIMATION_DURATION_SHORT),
        label = "elevation"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.3f else 0f,
        animationSpec = tween(300),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .width(DesignConstants.MENU_BUTTON_WIDTH)
            .height(DesignConstants.MENU_BUTTON_HEIGHT)
            .scale(scale)
            .background(
                if (isPrimary) {
                    Color(0xFF4ECDC4).copy(alpha = glowAlpha)
                } else {
                    Color(0xFF2C3E50).copy(alpha = glowAlpha)
                },
                shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS)
            )
            .padding(2.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            colors = CardDefaults.cardColors(
                containerColor = if (isPrimary) {
                    Color(0xFF4ECDC4)
                } else {
                    Color(0xFF2C3E50).copy(alpha = 0.8f)
                }
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        isPressed = true
                        onClick()
                    }
                    .padding(DesignConstants.ELEMENT_SPACING),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isPrimary) Color.White else Color(0xFF4ECDC4),
                        modifier = Modifier.size(DesignConstants.BUTTON_ICON_SIZE)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = text,
                        fontSize = DesignConstants.BUTTON_FONT_SIZE,
                        fontWeight = FontWeight.Medium,
                        color = if (isPrimary) Color.White else Color(0xFF4ECDC4)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedParticles() {
    val particles = remember { List(DesignConstants.PARTICLE_COUNT) { Particle() } }
    
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
            initialValue = 0.2f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration / 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration / 3, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        Box(
            modifier = Modifier
                .offset(x = xOffset.dp, y = yOffset.dp)
                .size(particle.size.dp)
                .scale(scale)
                .clip(RoundedCornerShape(DesignConstants.PARTICLE_CORNER_RADIUS))
                .background(
                    when (particle.type) {
                        ParticleType.TEAL -> Color(0xFF4ECDC4).copy(alpha = alpha)
                        ParticleType.ORANGE -> Color(0xFFFF6B6B).copy(alpha = alpha)
                        ParticleType.WHITE -> Color.White.copy(alpha = alpha * 0.5f)
                    },
                    shape = RoundedCornerShape(DesignConstants.PARTICLE_CORNER_RADIUS)
                )
        )
    }
}

private enum class ParticleType {
    TEAL, ORANGE, WHITE
}

private data class Particle(
    val initialX: Float = (0..400).random().toFloat(),
    val targetX: Float = (0..400).random().toFloat(),
    val initialY: Float = (0..800).random().toFloat(),
    val targetY: Float = (0..800).random().toFloat(),
    val duration: Int = (DesignConstants.PARTICLE_MIN_DURATION..DesignConstants.PARTICLE_MAX_DURATION).random(),
    val size: Float = DesignConstants.PARTICLE_MIN_SIZE + (DesignConstants.PARTICLE_MAX_SIZE - DesignConstants.PARTICLE_MIN_SIZE) * Random.nextFloat(),
    val type: ParticleType = ParticleType.values()[Random.nextInt(ParticleType.values().size)]
) 