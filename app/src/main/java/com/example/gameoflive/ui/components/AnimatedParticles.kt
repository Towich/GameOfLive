package com.example.gameoflive.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gameoflive.ui.theme.DesignConstants
import kotlin.random.Random

@Composable
fun AnimatedParticles() {
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

enum class ParticleType {
    TEAL, ORANGE, WHITE
}

data class Particle(
    val initialX: Float = (0..400).random().toFloat(),
    val targetX: Float = (0..400).random().toFloat(),
    val initialY: Float = (0..800).random().toFloat(),
    val targetY: Float = (0..800).random().toFloat(),
    val duration: Int = (DesignConstants.PARTICLE_MIN_DURATION..DesignConstants.PARTICLE_MAX_DURATION).random(),
    val size: Float = DesignConstants.PARTICLE_MIN_SIZE + (DesignConstants.PARTICLE_MAX_SIZE - DesignConstants.PARTICLE_MIN_SIZE) * Random.nextFloat(),
    val type: ParticleType = ParticleType.values()[Random.nextInt(ParticleType.values().size)]
) 