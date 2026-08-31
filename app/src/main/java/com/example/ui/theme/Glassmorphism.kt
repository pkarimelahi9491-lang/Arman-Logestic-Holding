package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ambient Glassmorphic Canvas Backdrop
 * Draws subtle ambient luminous orbs and a smooth linear gradient that brings
 * frosted glass translucency and depth to life across the application.
 */
@Composable
fun AmbientGlassBackdrop(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Base gradient canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            listOf(
                                CanvasDarkTop,
                                Color(0xFF0A1428),
                                CanvasDarkBottom
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                CanvasBgTop,
                                Color(0xFFF1F5F9),
                                CanvasBgBottom
                            )
                        )
                    }
                )
        )

        // Ambient specular glow orbs on canvas for refractive frosted glass depth
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (!isDark) {
                // Subtle top-right sapphire orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x182563EB),
                            Color(0x083B82F6),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.85f, h * 0.12f),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.85f, h * 0.12f),
                    radius = w * 0.65f
                )

                // Soft warm amber / gold glow at mid-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x14D97706),
                            Color(0x06F59E0B),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.12f, h * 0.45f),
                        radius = w * 0.55f
                    ),
                    center = Offset(w * 0.12f, h * 0.45f),
                    radius = w * 0.55f
                )

                // Gentle emerald tint at bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1010B981),
                            Color(0x04059669),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.80f, h * 0.88f),
                        radius = w * 0.60f
                    ),
                    center = Offset(w * 0.80f, h * 0.88f),
                    radius = w * 0.60f
                )
            } else {
                // Dark mode glowing accents
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x281E3A8A),
                            Color(0x0C1E40AF),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.8f, h * 0.15f),
                        radius = w * 0.7f
                    ),
                    center = Offset(w * 0.8f, h * 0.15f),
                    radius = w * 0.7f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x20B45309),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.15f, h * 0.6f),
                        radius = w * 0.6f
                    ),
                    center = Offset(w * 0.15f, h * 0.6f),
                    radius = w * 0.6f
                )
            }
        }

        // Content layer
        content()
    }
}

/**
 * Modern Clean Frosted Glass Card Component
 * Provides clean frosted glass effect with soft multi-stop gradient, specular highlight border, and clean corners.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurfaceLight,
    borderColor: Color = GlassBorderLight,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = shape,
                        ambientColor = NavyGlassBg.copy(alpha = 0.04f),
                        spotColor = NavyGlassBg.copy(alpha = 0.08f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.92f).coerceIn(0f, 1f))
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(BorderStroke(borderWidth, borderColor), shape),
        content = content
    )
}

/**
 * Dark Glass Card for Header, Hero sections, and Executive Badges
 */
@Composable
fun DarkGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = GlassSurfaceDark,
    borderColor: Color = GlassBorderDark,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0F1E36).copy(alpha = 0.94f),
                        Color(0xFF0A1424).copy(alpha = 0.98f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(BorderStroke(borderWidth, borderColor), shape),
        content = content
    )
}

/**
 * Sleek Frosted Glass Pill / Badge Container
 */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    backgroundColor: Color = Color(0x180F172A),
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(borderWidth, borderColor), shape),
        content = content
    )
}
