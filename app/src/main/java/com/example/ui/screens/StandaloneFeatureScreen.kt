package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class FeatureHeaderInfo(
    val title: String,
    val subtitle: String,
    val accentColor: Color
)

fun getFeatureHeaderInfo(featureKey: String): FeatureHeaderInfo = when (featureKey) {
    "calc_0" -> FeatureHeaderInfo("RESISTOR COLOR CODE", "4, 5 & 6-Band Graphic Analyzer", ThemeAccentCyan)
    "calc_1" -> FeatureHeaderInfo("SMD COMPONENT TESTER", "Surface Mount Code & Precision EIA-96", ThemeAccentGreen)
    "calc_2" -> FeatureHeaderInfo("CAPACITOR CODE DECODER", "EIA 3-Digit & Multi-Unit Converter", ThemeAccentGold)
    "calc_3" -> FeatureHeaderInfo("555 TIMER STUDIO", "Oscillator & Live Waveform Generator", Color(0xFF818CF8))
    "calc_4" -> FeatureHeaderInfo("LED RESISTOR DESIGNER", "Current Limiting & E12 Safety Values", Color(0xFFF43F5E))
    "calc_5" -> FeatureHeaderInfo("OHM'S & POWER LAW", "4-Parameter Dynamic Electrical Solver", Color(0xFF38BDF8))
    "ref_0" -> FeatureHeaderInfo("BOARD PINOUT MAPS", "Arduino, ESP32, NodeMCU & Pi Pico", ThemeAccentGreen)
    "ref_1" -> FeatureHeaderInfo("LOGIC GATE SIMULATOR", "Interactive Logic Gates & Live Truth Table", ThemeAccentCyan)
    "ref_2" -> FeatureHeaderInfo("FORMULAS & LAWS", "Circuit Principles & Mathematical Guides", ThemeAccentGold)
    "components" -> FeatureHeaderInfo("COMPONENT PINOUT CATALOG", "Semiconductor Directory & Specs", Color(0xFFA855F7))
    else -> FeatureHeaderInfo("ELECTRO MASTER TOOL", "Engineering Module", ThemePrimary)
}

@Composable
fun StandaloneTopBar(
    featureKey: String,
    onBackToHome: () -> Unit,
    onOpenThemeDialog: () -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val featureInfo = getFeatureHeaderInfo(featureKey)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ThemeSurface,
        border = BorderStroke(1.dp, ThemeOutline),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back to Home Button
            OutlinedButton(
                onClick = onBackToHome,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ThemePrimary.copy(alpha = 0.1f)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("back_to_home_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = ThemePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ThemePrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = featureInfo.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                Text(
                    text = featureInfo.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = featureInfo.accentColor,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Theme Switcher Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ThemePrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenThemeDialog() }
                        .testTag("standalone_theme_button")
                ) {
                    Icon(
                        Icons.Outlined.Palette,
                        contentDescription = "Change Theme",
                        tint = ThemePrimary,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }

                // Settings Button if handler provided
                if (onNavigateToSettings != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ThemeSurfaceElevated,
                        border = BorderStroke(1.dp, ThemeOutline),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToSettings() }
                            .testTag("standalone_settings_button")
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(16.dp)
                        )
                    }
                }

                // Dedicated Mode Indicator Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = featureInfo.accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, featureInfo.accentColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "FOCUSED",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = featureInfo.accentColor
                    )
                }
            }
        }
    }
}

@Composable
fun StandaloneFeatureScreen(
    featureKey: String,
    onBackToHome: () -> Unit,
    onOpenThemeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Hardware and gesture back handler returns to Home
    BackHandler {
        onBackToHome()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        when (featureKey) {
            "calc_0" -> ResistorColorCodeTool()
            "calc_1" -> SmdResistorTesterTool()
            "calc_2" -> CapacitorCodeDecoderTool()
            "calc_3" -> Timer555StudioTool()
            "calc_4" -> LedResistorDesignerTool()
            "calc_5" -> OhmsLawStudioTool()
            "ref_0" -> InteractiveBoardPinoutsSection()
            "ref_1" -> InteractiveLogicGateSimulatorSection()
            "ref_2" -> AdvancedFormulasSection()
            "components" -> SearchScreen()
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Feature under construction",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
