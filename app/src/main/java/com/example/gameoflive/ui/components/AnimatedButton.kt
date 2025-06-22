package com.example.gameoflive.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameoflive.ui.theme.DesignConstants

@Composable
fun AnimatedButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: androidx.compose.ui.unit.Dp = DesignConstants.MENU_BUTTON_WIDTH,
    height: androidx.compose.ui.unit.Dp = DesignConstants.MENU_BUTTON_HEIGHT
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
        modifier = modifier
            .width(width)
            .height(height)
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
                    .clickable(enabled = enabled) {
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
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = if (isPrimary) Color.White else Color(0xFF4ECDC4),
                            modifier = Modifier.size(DesignConstants.BUTTON_ICON_SIZE)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
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