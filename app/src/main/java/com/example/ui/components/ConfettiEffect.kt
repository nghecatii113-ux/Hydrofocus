package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val xRatio: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val wobbleFrequency: Float
)

@Composable
fun ConfettiEffect(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFF00B4D8),
            Color(0xFFFFB703),
            Color(0xFFFB8500),
            Color(0xFF2EC4B6),
            Color(0xFFFF5A5F),
            Color(0xFF90E0EF),
            Color(0xFF7209B7)
        )
        List(70) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                speed = 0.5f + Random.nextFloat() * 0.8f,
                size = 10f + Random.nextFloat() * 14f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                wobbleFrequency = 2f + Random.nextFloat() * 4f
            )
        }
    }

    LaunchedEffect(isVisible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val currentProgress = progress.value
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { particle ->
            val yPos = (currentProgress * particle.speed * canvasHeight * 1.2f) - 50f
            val wobble = kotlin.math.sin(currentProgress * particle.wobbleFrequency * Math.PI.toFloat()) * 30f
            val xPos = (particle.xRatio * canvasWidth) + wobble
            val alpha = (1f - (currentProgress - 0.7f) / 0.3f).coerceIn(0f, 1f)

            if (yPos in -50f..canvasHeight) {
                rotate(
                    degrees = currentProgress * particle.rotationSpeed,
                    pivot = Offset(xPos, yPos)
                ) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(xPos, yPos),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
