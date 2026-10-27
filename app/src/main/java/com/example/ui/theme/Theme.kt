package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val theme = ThemeManager.currentTheme
    val palette = ThemeManager.currentPalette

    val colorScheme = if (theme == AppTheme.WHITE) {
        lightColorScheme(
            background = palette.background,
            surface = palette.surface,
            surfaceVariant = palette.surfaceVariant,
            onBackground = palette.onBackground,
            onSurface = palette.onSurface,
            onSurfaceVariant = palette.onSurfaceVariant,
            primary = palette.primary,
            primaryContainer = palette.primaryContainer,
            onPrimaryContainer = palette.onPrimaryContainer,
            secondary = palette.secondary,
            secondaryContainer = palette.secondaryContainer,
            onSecondaryContainer = palette.onSecondaryContainer,
            outline = palette.outline,
            outlineVariant = palette.outlineBright,
            surfaceContainer = palette.navigationBar,
            surfaceContainerHigh = palette.surfaceCard,
            surfaceContainerHighest = palette.surfaceElevated,
            tertiary = palette.accentGreen
        )
    } else {
        darkColorScheme(
            background = palette.background,
            surface = palette.surface,
            surfaceVariant = palette.surfaceVariant,
            onBackground = palette.onBackground,
            onSurface = palette.onSurface,
            onSurfaceVariant = palette.onSurfaceVariant,
            primary = palette.primary,
            primaryContainer = palette.primaryContainer,
            onPrimaryContainer = palette.onPrimaryContainer,
            secondary = palette.secondary,
            secondaryContainer = palette.secondaryContainer,
            onSecondaryContainer = palette.onSecondaryContainer,
            outline = palette.outline,
            outlineVariant = palette.outlineBright,
            surfaceContainer = palette.navigationBar,
            surfaceContainerHigh = palette.surfaceCard,
            surfaceContainerHighest = palette.surfaceElevated,
            tertiary = palette.accentGreen
        )
    }

    CompositionLocalProvider(
        LocalAppTheme provides theme,
        LocalAppPalette provides palette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Reusable full-screen background container that renders:
 * - A rich multi-stop aurora mesh for LIQUID_GLASS
 * - A subtle hardware-accelerated radial ambient glow at the top-right and top-left corners
 *   using ThemePrimary.copy(alpha = 0.08f) and ThemeSecondary.copy(alpha = 0.06f) over ThemeBackground
 *   for all dark themes (DARK, BLACK_WHITE)
 * - Clean solid background for WHITE theme
 */
@Composable
fun AppBackgroundContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalAppPalette.current
    val theme = LocalAppTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        if (palette.isLiquidGlass) {
            // Rich multi-stop aurora mesh for LIQUID_GLASS
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Base deep cosmic indigo wash
                drawRect(color = Color(0xFF060919))

                // Top-right luminous aqua glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF38F9D7).copy(alpha = 0.22f),
                            Color(0xFF00E5FF).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.92f, h * 0.08f),
                        radius = w * 0.75f
                    ),
                    radius = w * 0.75f,
                    center = Offset(w * 0.92f, h * 0.08f)
                )

                // Top-left violet orchid glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFC084FC).copy(alpha = 0.18f),
                            Color(0xFF7C3AED).copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.08f, h * 0.32f),
                        radius = w * 0.8f
                    ),
                    radius = w * 0.8f,
                    center = Offset(w * 0.08f, h * 0.32f)
                )

                // Center-bottom deep cosmic sapphire glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A).copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.88f),
                        radius = w * 0.85f
                    ),
                    radius = w * 0.85f,
                    center = Offset(w * 0.5f, h * 0.88f)
                )
            }
        } else if (theme.isDark) {
            // Subtle hardware-accelerated radial ambient glow at top-right and top-left
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Top-right ambient primary glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.95f, 0f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.95f, 0f)
                )

                // Top-left ambient secondary glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.secondary.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.05f, h * 0.18f),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.55f,
                    center = Offset(w * 0.05f, h * 0.18f)
                )
            }
        }

        // Child content rendered over the layered ambient canvas
        content()
    }
}
