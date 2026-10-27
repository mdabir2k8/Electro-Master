package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

fun getThemeIcon(theme: AppTheme): ImageVector {
    return when (theme) {
        AppTheme.DARK -> Icons.Outlined.DarkMode
        AppTheme.BLACK_WHITE -> Icons.Outlined.Contrast
        AppTheme.LIQUID_GLASS -> Icons.Outlined.WaterDrop
        AppTheme.WHITE -> Icons.Outlined.LightMode
    }
}

fun getThemePreviewColors(theme: AppTheme): List<Color> {
    return when (theme) {
        AppTheme.DARK -> listOf(
            DarkPalette.background,
            DarkPalette.surfaceCard,
            DarkPalette.primary,
            DarkPalette.secondary
        )
        AppTheme.BLACK_WHITE -> listOf(
            BlackWhitePalette.background,
            BlackWhitePalette.surfaceCard,
            BlackWhitePalette.primary,
            BlackWhitePalette.secondary
        )
        AppTheme.LIQUID_GLASS -> listOf(
            LiquidGlassPalette.background,
            LiquidGlassPalette.surfaceCard,
            LiquidGlassPalette.primary,
            LiquidGlassPalette.secondary
        )
        AppTheme.WHITE -> listOf(
            WhitePalette.background,
            WhitePalette.surface,
            WhitePalette.primary,
            WhitePalette.secondary
        )
    }
}

/**
 * Interactive Dialog to view and switch between all 4 supported themes:
 * 1. Dark theme
 * 2. Black and white mixed
 * 3. Liquid glass
 * 4. White theme
 */
@Composable
fun ThemeSelectorDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = ThemeManager.currentTheme

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = ThemeSurface,
            border = BorderStroke(1.dp, ThemeOutlineBright),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ThemePrimary.copy(alpha = 0.16f))
                                .border(1.dp, ThemePrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Palette,
                                contentDescription = "Theme",
                                tint = ThemePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "SELECT APP THEME",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "4 Engineered Styles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // The 4 Theme Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppTheme.entries.forEach { themeOption ->
                        val isSelected = currentTheme == themeOption
                        val icon = getThemeIcon(themeOption)
                        val previewColors = getThemePreviewColors(themeOption)

                        val animatedBorderColor by animateColorAsState(
                            targetValue = if (isSelected) ThemePrimary else ThemeOutline,
                            label = "border_color"
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) ThemePrimary.copy(alpha = 0.14f) else ThemeSurfaceCard,
                            border = BorderStroke(
                                if (isSelected) 1.8.dp else 1.dp,
                                animatedBorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("theme_option_${themeOption.id}")
                                .clickable {
                                    ThemeManager.setTheme(themeOption, context)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) ThemePrimary else ThemeSurfaceElevated
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = themeOption.title,
                                            tint = if (isSelected) {
                                                if (themeOption == AppTheme.WHITE) Color.White else ThemeBackground
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = themeOption.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = ThemePrimary.copy(alpha = 0.2f),
                                                    border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.6f))
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ThemePrimary,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = themeOption.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Color Swatches
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            previewColors.forEach { color ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                        .border(0.8.dp, ThemeOutlineBright, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(ThemePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = if (themeOption == AppTheme.WHITE) Color.White else ThemeBackground,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("close_theme_dialog"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemePrimary
                    )
                ) {
                    Text(
                        text = "Apply & Done",
                        fontWeight = FontWeight.Bold,
                        color = if (currentTheme == AppTheme.WHITE) Color.White else ThemeBackground
                    )
                }
            }
        }
    }
}

/**
 * Compact horizontal quick theme switcher pills that can be placed on the Home screen.
 */
@Composable
fun ThemeQuickSelectorRow(
    onOpenFullDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme = ThemeManager.currentTheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = ThemePrimary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "THEME STYLES",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
            }

            Text(
                text = "Customize",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ThemePrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onOpenFullDialog() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("theme_customize_link")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AppTheme.entries, key = { it.id }) { theme ->
                val isSelected = currentTheme == theme
                val icon = getThemeIcon(theme)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) ThemePrimary.copy(alpha = 0.18f) else ThemeSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) ThemePrimary else ThemeOutline
                    ),
                    modifier = Modifier
                        .testTag("quick_theme_${theme.id}")
                        .clickable {
                            ThemeManager.setTheme(theme, context)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = theme.title,
                            tint = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when (theme) {
                                AppTheme.DARK -> "Cyber-Lab"
                                AppTheme.BLACK_WHITE -> "PCB Gold"
                                AppTheme.LIQUID_GLASS -> "Liquid Glass"
                                AppTheme.WHITE -> "Alabaster"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ThemePrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}
