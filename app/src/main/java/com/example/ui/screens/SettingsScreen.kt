package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.theme.*

private val SectionCardShape = RoundedCornerShape(16.dp)
private val ChipButtonShape = RoundedCornerShape(10.dp)
private val SettingRowShape = RoundedCornerShape(12.dp)

@Composable
fun SettingsScreen(
    onOpenThemeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Screen Header
        SettingsHeader()

        // 2. Appearance & Theme Selection
        ThemeSettingsCard(
            onOpenThemeDialog = onOpenThemeDialog
        )

        // 3. Lab Hardware & Display Controls
        HardwareDisplayCard()

        // 4. Engineering Units & Precision
        CalculationPreferencesCard()

        // 5. Offline Database & System Status
        DatabaseStatusCard(
            onResetClicked = { showResetDialog = true }
        )

        // 6. About App Card
        AboutAppCard()

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "Reset Preferences?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "This will restore all calculation units, precision (2 decimal places), and screen wake settings to default values. Your saved theme will not be affected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        UserSettings.resetToDefaults(context)
                        showResetDialog = false
                        Toast.makeText(context, "Settings restored to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_reset_settings")
                ) {
                    Text("Reset", color = ThemeManager.currentPalette.onPrimaryContainer)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    modifier = Modifier.testTag("cancel_reset_settings")
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = ThemeSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsHeader() {
    Surface(
        shape = SectionCardShape,
        color = ThemeSurface,
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ThemePrimary.copy(alpha = 0.15f))
                    .border(1.dp, ThemePrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = ThemePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LAB WORKSPACE SETTINGS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customize theme, hardware sleep, calculation units & offline lab",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ThemeSettingsCard(
    onOpenThemeDialog: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = ThemeManager.currentTheme

    Card(
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_theme_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = ThemePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "THEME & VISUAL STYLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ThemePrimary,
                        letterSpacing = 1.sp
                    )
                }

                TextButton(
                    onClick = onOpenThemeDialog,
                    modifier = Modifier.testTag("settings_open_theme_modal")
                ) {
                    Text(
                        "More Info",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Distinct Theme Quick Selection Tiles
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.values().forEach { themeOption ->
                    val isSelected = currentTheme == themeOption
                    val previewColors = when (themeOption) {
                        AppTheme.DARK -> listOf(DarkPalette.background, DarkPalette.primary, DarkPalette.secondary)
                        AppTheme.BLACK_WHITE -> listOf(BlackWhitePalette.background, BlackWhitePalette.primary, BlackWhitePalette.secondary)
                        AppTheme.LIQUID_GLASS -> listOf(LiquidGlassPalette.background, LiquidGlassPalette.primary, LiquidGlassPalette.secondary)
                        AppTheme.WHITE -> listOf(WhitePalette.background, WhitePalette.primary, WhitePalette.secondary)
                    }

                    Surface(
                        shape = SettingRowShape,
                        color = if (isSelected) ThemePrimary.copy(alpha = 0.16f) else ThemeSurfaceElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ThemePrimary else ThemeOutline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                ThemeManager.setTheme(themeOption, context)
                            }
                            .testTag("settings_theme_tile_${themeOption.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Theme indicator swatches
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    previewColors.forEach { c ->
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = themeOption.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = themeOption.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ThemePrimary,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = ThemeManager.currentPalette.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareDisplayCard() {
    val context = LocalContext.current

    Card(
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_hardware_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = ThemeAccentGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "HARDWARE & WORKBENCH DISPLAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ThemeAccentGreen,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Keep Screen Awake Switch
            SettingsSwitchRow(
                title = "Keep Screen Awake (Wake Lock)",
                subtitle = "Prevents screen timeout during oscilloscope and breadboard work",
                icon = Icons.Outlined.Visibility,
                isChecked = UserSettings.keepScreenOn,
                onCheckedChange = { UserSettings.setKeepScreenOn(it, context) },
                testTag = "setting_toggle_screen_on"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = ThemeOutline.copy(alpha = 0.5f)
            )

            // Haptic Feedback Switch
            SettingsSwitchRow(
                title = "Haptic Tactile Clicks",
                subtitle = "Vibrate softly when toggling logic gate switches and pins",
                icon = Icons.Outlined.Vibration,
                isChecked = UserSettings.hapticFeedback,
                onCheckedChange = { UserSettings.setHapticFeedback(it, context) },
                testTag = "setting_toggle_haptics"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = ThemeOutline.copy(alpha = 0.5f)
            )

            // Show Formula Equations Switch
            SettingsSwitchRow(
                title = "Display Formula Cheatsheets",
                subtitle = "Show underlying mathematical equations in calculator cards",
                icon = Icons.Outlined.Functions,
                isChecked = UserSettings.showFormulasAlways,
                onCheckedChange = { UserSettings.setShowFormulasAlways(it, context) },
                testTag = "setting_toggle_formulas"
            )
        }
    }
}

@Composable
private fun CalculationPreferencesCard() {
    val context = LocalContext.current

    Card(
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_calculation_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Calculate,
                    contentDescription = null,
                    tint = ThemeAccentAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ENGINEERING UNITS & PRECISION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ThemeAccentAmber,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Decimal Precision Picker
            Text(
                text = "Calculator Decimal Precision",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Controls rounding on Ohm's law, 555 timers, and LED resistors",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 3, 4).forEach { precision ->
                    val isSelected = UserSettings.decimalPrecision == precision
                    Surface(
                        shape = ChipButtonShape,
                        color = if (isSelected) ThemePrimary else ThemeSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) ThemePrimary else ThemeOutline),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { UserSettings.setDecimalPrecision(precision, context) }
                            .testTag("setting_precision_$precision")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${precision} DP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ThemeManager.currentPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (precision) {
                                    1 -> "0.0"
                                    2 -> "0.00"
                                    3 -> "0.000"
                                    else -> "0.0000"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) ThemeManager.currentPalette.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature Scale
            Text(
                text = "Thermal Temperature Unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("°C (Celsius)", "°F (Fahrenheit)", "K (Kelvin)").forEach { unitLabel ->
                    val unitKey = unitLabel.substringBefore(" ")
                    val isSelected = UserSettings.tempUnit == unitKey

                    Surface(
                        shape = ChipButtonShape,
                        color = if (isSelected) ThemePrimary.copy(alpha = 0.2f) else ThemeSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) ThemePrimary else ThemeOutline),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { UserSettings.setTempUnit(unitKey, context) }
                            .testTag("setting_temp_${unitKey}")
                    ) {
                        Text(
                            text = unitLabel,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Default Resistor Tolerance
            Text(
                text = "Default Resistor Tolerance Band",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1% (Brown)", "5% (Gold)", "10% (Silver)").forEach { tol ->
                    val isSelected = UserSettings.defaultResistorTolerance == tol

                    Surface(
                        shape = ChipButtonShape,
                        color = if (isSelected) ThemePrimary.copy(alpha = 0.2f) else ThemeSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) ThemePrimary else ThemeOutline),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { UserSettings.setDefaultResistorTolerance(tol, context) }
                            .testTag("setting_tol_${tol.take(2)}")
                    ) {
                        Text(
                            text = tol,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatabaseStatusCard(
    onResetClicked: () -> Unit
) {
    Card(
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_database_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Storage,
                        contentDescription = null,
                        tint = ThemeAccentPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "DATA & STORAGE ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ThemeAccentPurple,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ThemeAccentGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ThemeAccentGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "HEALTHY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ThemeAccentGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = SettingRowShape,
                color = ThemeSurfaceElevated,
                border = BorderStroke(1.dp, ThemeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Component Database",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "300+ Parts Preloaded",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Persistence Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Local SQLite (Room)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = ThemePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Network Dependency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "0 KB (Pure Offline)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = ThemeAccentGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onResetClicked,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ThemeAccentRed.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeAccentRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_reset_button")
            ) {
                Icon(
                    Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Reset Preferences to Factory Defaults",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                )
            }
        }
    }
}

@Composable
private fun AboutAppCard() {
    Card(
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_about_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ThemePrimary.copy(alpha = 0.15f))
                    .border(1.dp, ThemePrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Memory,
                    contentDescription = "Logo",
                    tint = ThemePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ELECTRO MASTER PRO",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Version 1.0 • Lab Edition",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ThemeSurfaceElevated,
                border = BorderStroke(0.8.dp, ThemeOutline)
            ) {
                Text(
                    text = "Built with Kotlin & Jetpack Compose M3",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ThemeSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isChecked) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .padding(start = 8.dp)
                .testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = ThemeManager.currentPalette.onPrimaryContainer,
                checkedTrackColor = ThemePrimary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = ThemeSurfaceElevated
            )
        )
    }
}
