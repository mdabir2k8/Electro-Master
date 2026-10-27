package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// Pre-allocated static shapes to avoid GC pauses during scroll
private val CommandPillShape = RoundedCornerShape(22.dp)
private val BentoCardShape = RoundedCornerShape(16.dp)
private val IconBoxShape = RoundedCornerShape(10.dp)
private val MicroTagShape = RoundedCornerShape(4.dp)
private val CategoryPillShape = RoundedCornerShape(10.dp)
private val CountBadgeShape = RoundedCornerShape(6.dp)

@Immutable
data class AppFeatureItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badges: List<String>,
    val description: String,
    val route: String
)

val allAppFeatures = listOf(
    AppFeatureItem(
        id = "resistor_color",
        title = "Resistor Color Code",
        subtitle = "4, 5 & 6-Band Graphic Analyzer",
        category = "Calculators",
        icon = Icons.Outlined.Palette,
        accentColor = Color(0xFF06B6D4),
        badges = listOf("Color Bands", "PPM/K"),
        description = "Interactive color band selector with live graphic resistor rendering, multiplier lookup, and precision tolerance calculations.",
        route = "feature_calc/0"
    ),
    AppFeatureItem(
        id = "smd_tester",
        title = "SMD Component Tester",
        subtitle = "Surface Mount Code Decoder",
        category = "Calculators",
        icon = Icons.Outlined.Memory,
        accentColor = Color(0xFF10B981),
        badges = listOf("3/4 Digit", "EIA-96"),
        description = "Decode surface mount resistors and chips with real-time ceramic SMD visual graphic and commercial tolerance verification.",
        route = "feature_calc/1"
    ),
    AppFeatureItem(
        id = "capacitor_code",
        title = "Capacitor Code Decoder",
        subtitle = "EIA 3-Digit & Unit Converter",
        category = "Calculators",
        icon = Icons.Outlined.ElectricBolt,
        accentColor = Color(0xFFF59E0B),
        badges = listOf("pF • nF • µF", "Ceramic"),
        description = "Instant 3-digit marking decoder with multi-unit conversions (pF, nF, µF) and letter tolerance ratings.",
        route = "feature_calc/2"
    ),
    AppFeatureItem(
        id = "timer_555",
        title = "555 Timer Studio",
        subtitle = "Oscillator & Pulse Designer",
        category = "Calculators",
        icon = Icons.Outlined.Timer,
        accentColor = Color(0xFF818CF8),
        badges = listOf("Astable/Mono", "Oscilloscope"),
        description = "Compute oscillation frequencies, timing intervals, and duty cycles with real-time dynamic oscilloscope square wave display.",
        route = "feature_calc/3"
    ),
    AppFeatureItem(
        id = "led_resistor",
        title = "LED Series Resistor",
        subtitle = "Current Limiting & Safety Values",
        category = "Calculators",
        icon = Icons.Outlined.Lightbulb,
        accentColor = Color(0xFFF43F5E),
        badges = listOf("Current Limit", "E12 Series"),
        description = "Design LED drive circuits with supply voltage presets, commercial standard E12 values, and safety thermal ratings.",
        route = "feature_calc/4"
    ),
    AppFeatureItem(
        id = "ohms_law",
        title = "Ohm's & Power Law",
        subtitle = "4-Parameter Dynamic Solver",
        category = "Calculators",
        icon = Icons.Outlined.Speed,
        accentColor = Color(0xFF38BDF8),
        badges = listOf("V • I • R • P", "Formulas"),
        description = "Dynamic live wheel solver: enter any two parameters to automatically calculate the remaining two with power formulas.",
        route = "feature_calc/5"
    ),
    AppFeatureItem(
        id = "microcontroller_pinouts",
        title = "Board Pinout Maps",
        subtitle = "Interactive Development Boards",
        category = "Pinouts & Hardware",
        icon = Icons.Outlined.DeveloperBoard,
        accentColor = Color(0xFF10B981),
        badges = listOf("Arduino • ESP32", "STM32 • Pico"),
        description = "Full interactive microcontroller board pinouts with category filters (Power, Digital, ADC, I2C/SPI) and quick search.",
        route = "feature_ref/0"
    ),
    AppFeatureItem(
        id = "logic_simulator",
        title = "Logic Gate Simulator",
        subtitle = "Interactive Digital Sandbox",
        category = "Simulation & Logic",
        icon = Icons.Outlined.AccountTree,
        accentColor = Color(0xFF06B6D4),
        badges = listOf("7 Gate Types", "Truth Table"),
        description = "Interactive logic simulator for AND, OR, NOT, NAND, NOR, XOR, XNOR gates with live switch inputs and glowing output LED.",
        route = "feature_ref/1"
    ),
    AppFeatureItem(
        id = "formulas_reference",
        title = "Formulas & Laws",
        subtitle = "Engineering Theory & Cheatsheet",
        category = "Theory & Reference",
        icon = Icons.Outlined.AutoStories,
        accentColor = Color(0xFFFBBF24),
        badges = listOf("Formulas", "Equations"),
        description = "Core electronic engineering principles, mathematical equations, variable definitions, and standard unit guides.",
        route = "feature_ref/2"
    ),
    AppFeatureItem(
        id = "component_catalog",
        title = "Component Directory",
        subtitle = "300+ Lab & Thesis Components",
        category = "Components",
        icon = Icons.Outlined.Widgets,
        accentColor = Color(0xFFA855F7),
        badges = listOf("300+ Items", "Pinouts & Specs"),
        description = "Comprehensive encyclopedia of 300+ components (ESP32, STM32, Sensors, ICs, Transistors, MOSFETs, Regulators, Motor Drivers, Relays, Displays) with pinouts & specs.",
        route = "feature_components"
    ),
    AppFeatureItem(
        id = "lab_settings",
        title = "Lab Settings & Config",
        subtitle = "Preferences, Precision, Themes & Screen",
        category = "Settings",
        icon = Icons.Outlined.Tune,
        accentColor = Color(0xFF38BDF8),
        badges = listOf("Themes", "Wake Lock"),
        description = "Configure lab preferences, calculator decimal precision, screen wake lock during breadboard assembly, themes, and check offline database status.",
        route = "settings"
    )
)

private data class HomeCategory(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private val homeCategories = listOf(
    HomeCategory("All", "All", Icons.Outlined.Widgets),
    HomeCategory("Calculators", "Calculators", Icons.Outlined.Calculate),
    HomeCategory("Pinouts & Hardware", "Pinouts", Icons.Outlined.DeveloperBoard),
    HomeCategory("Simulation & Logic", "Logic", Icons.Outlined.AccountTree),
    HomeCategory("Theory & Reference", "Theory", Icons.Outlined.AutoStories),
    HomeCategory("Components", "Components", Icons.Outlined.Memory),
    HomeCategory("Settings", "Settings", Icons.Outlined.Tune)
)

// Resistor Band Color Models for Interactive Left Hero Card
private data class ResistorBand(val name: String, val color: Color, val value: Int, val multiplier: Double = 1.0)

private val digitBands = listOf(
    ResistorBand("Black", Color(0xFF262626), 0),
    ResistorBand("Brown", Color(0xFF8D6E63), 1),
    ResistorBand("Red", Color(0xFFEF5350), 2),
    ResistorBand("Orange", Color(0xFFFFA726), 3),
    ResistorBand("Yellow", Color(0xFFFFEE58), 4),
    ResistorBand("Green", Color(0xFF66BB6A), 5),
    ResistorBand("Blue", Color(0xFF42A5F5), 6),
    ResistorBand("Violet", Color(0xFFAB47BC), 7),
    ResistorBand("Grey", Color(0xFF9E9E9E), 8),
    ResistorBand("White", Color(0xFFFAFAFA), 9)
)

private val multiplierBands = listOf(
    ResistorBand("Black", Color(0xFF262626), 0, 1.0),
    ResistorBand("Brown", Color(0xFF8D6E63), 1, 10.0),
    ResistorBand("Red", Color(0xFFEF5350), 2, 100.0),
    ResistorBand("Orange", Color(0xFFFFA726), 3, 1_000.0),
    ResistorBand("Yellow", Color(0xFFFFEE58), 4, 10_000.0),
    ResistorBand("Green", Color(0xFF66BB6A), 5, 100_000.0),
    ResistorBand("Blue", Color(0xFF42A5F5), 6, 1_000_000.0),
    ResistorBand("Gold", Color(0xFFFFD700), -1, 0.1),
    ResistorBand("Silver", Color(0xFFC0C0C0), -2, 0.01)
)

private val toleranceBands = listOf(
    ResistorBand("Gold", Color(0xFFFFD700), 5),
    ResistorBand("Silver", Color(0xFFC0C0C0), 10),
    ResistorBand("Brown", Color(0xFF8D6E63), 1),
    ResistorBand("Red", Color(0xFFEF5350), 2)
)

@Composable
fun HomeScreen(
    onNavigateToFeature: (String) -> Unit,
    onOpenThemeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isSearchFocused by remember { mutableStateOf(false) }

    // Interactive Hero Resistor State (default: 10.0 kΩ ±5%)
    var band1Idx by remember { mutableIntStateOf(1) } // Brown = 1
    var band2Idx by remember { mutableIntStateOf(0) } // Black = 0
    var multIdx by remember { mutableIntStateOf(3) }  // Orange = 1k
    var tolIdx by remember { mutableIntStateOf(0) }   // Gold = 5%

    val categoryCounts = remember {
        homeCategories.associate { cat ->
            cat.key to if (cat.key == "All") allAppFeatures.size else allAppFeatures.count { it.category == cat.key }
        }
    }

    val filteredFeatures = remember(searchQuery, selectedCategory) {
        allAppFeatures.filter { feature ->
            val matchesCategory = (selectedCategory == "All") || (feature.category == selectedCategory)
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    feature.title.lowercase().contains(query) ||
                    feature.subtitle.lowercase().contains(query) ||
                    feature.description.lowercase().contains(query) ||
                    feature.badges.any { it.lowercase().contains(query) }
            matchesCategory && matchesQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        // 1. Precision Dot-Matrix Canvas Background & Ambient Glow
        val dotMatrixColor = ThemeOutline.copy(alpha = 0.22f)
        val ambientPrimary = ThemePrimary.copy(alpha = 0.09f)
        val ambientSecondary = ThemeSecondary.copy(alpha = 0.07f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotRadius = 1.2.dp.toPx()
            val spacing = 22.dp.toPx()

            // Top-left radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambientPrimary, Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(0f, 0f)
            )

            // Top-right radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambientSecondary, Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width, 0f)
            )

            // Dot-matrix grid pattern
            var x = spacing / 2
            while (x < size.width) {
                var y = spacing / 2
                while (y < size.height) {
                    drawCircle(
                        color = dotMatrixColor,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    y += spacing
                }
                x += spacing
            }
        }

        // Main Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 2. Command-Pill Search Bar with View Switcher
            item {
                val searchBorderBrush = if (isSearchFocused) {
                    Brush.horizontalGradient(listOf(ThemePrimary, ThemeSecondary))
                } else {
                    Brush.horizontalGradient(listOf(ThemeOutline, ThemeOutline))
                }

                Surface(
                    shape = CommandPillShape,
                    color = ThemeSurface,
                    border = BorderStroke(1.2.dp, searchBorderBrush),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (isSearchFocused) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Command / Search 11 lab modules...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isSearchFocused = it.isFocused }
                                .testTag("home_search_input"),
                            shape = CommandPillShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                            ),
                            singleLine = true
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    if (UserSettings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Grid / List View Toggle Icon Button
                        IconButton(
                            onClick = {
                                val newMode = !UserSettings.isCompactGridView
                                UserSettings.setCompactGridView(newMode, context)
                                if (UserSettings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (UserSettings.isCompactGridView) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                                contentDescription = if (UserSettings.isCompactGridView) "Switch to Detailed List" else "Switch to Bento Grid",
                                tint = ThemePrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // 3. Glowing Category Dock (Placed directly beneath Search Bar)
            item {
                val chipsScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipsScrollState)
                        .padding(vertical = 2.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    homeCategories.forEach { categoryItem ->
                        val isSelected = selectedCategory == categoryItem.key
                        val count = categoryCounts[categoryItem.key] ?: 0

                        Surface(
                            shape = CategoryPillShape,
                            color = if (isSelected) ThemePrimary.copy(alpha = 0.16f) else ThemeSurface,
                            border = BorderStroke(
                                if (isSelected) 1.2.dp else 1.dp,
                                if (isSelected) ThemePrimary else ThemeOutline
                            ),
                            modifier = Modifier
                                .testTag("home_category_${categoryItem.key}")
                                .clickable {
                                    selectedCategory = categoryItem.key
                                    if (UserSettings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    categoryItem.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = categoryItem.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = CountBadgeShape,
                                    color = if (isSelected) ThemePrimary.copy(alpha = 0.28f) else ThemeSurfaceElevated,
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Interactive Asymmetric Bento Hero Dock (When not searching & Category == 'All')
            if (searchQuery.isEmpty() && selectedCategory == "All") {
                item {
                    val rawOhms = (digitBands[band1Idx].value * 10 + digitBands[band2Idx].value) * multiplierBands[multIdx].multiplier
                    val tolerancePct = toleranceBands[tolIdx].value
                    val resistanceFormatted = formatResistorValue(rawOhms, tolerancePct)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(196.dp)
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Tall Interactive Card: Resistor Quick Decoder (Weight 1.05f)
                        Card(
                            shape = BentoCardShape,
                            colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                            border = BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(Color(0xFF06B6D4).copy(alpha = 0.55f), ThemeOutline))
                            ),
                            modifier = Modifier
                                .weight(1.05f)
                                .fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.16f), Color.Transparent),
                                            center = Offset(0f, 0f),
                                            radius = 240f
                                        )
                                    )
                                    .padding(11.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Title row with direct jump to full calculator
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToFeature("feature_calc/0") },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "RESISTOR DECODER",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = Color(0xFF06B6D4),
                                                letterSpacing = 0.8.sp
                                            )
                                            Text(
                                                text = "Live 4-Band Studio",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.5.sp
                                            )
                                        }

                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Open Resistor Calculator",
                                            tint = Color(0xFF06B6D4),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    // Canvas: Glowing Axial Resistor Graphic
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                    ) {
                                        val w = size.width
                                        val h = size.height
                                        val centerY = h / 2f
                                        val leadY = centerY

                                        // Metal Lead Wires
                                        drawLine(
                                            color = Color(0xFF94A3B8),
                                            start = Offset(4f, leadY),
                                            end = Offset(w - 4f, leadY),
                                            strokeWidth = 2.5f.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )

                                        // Ceramic Resistor Body with bulbous ends
                                        val bodyStart = w * 0.18f
                                        val bodyEnd = w * 0.82f
                                        val bodyWidth = bodyEnd - bodyStart
                                        val bodyHeight = h * 0.68f
                                        val bodyTop = centerY - bodyHeight / 2f

                                        drawRoundRect(
                                            color = Color(0xFF1E293B),
                                            topLeft = Offset(bodyStart, bodyTop),
                                            size = Size(bodyWidth, bodyHeight),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )

                                        drawRoundRect(
                                            color = Color(0xFF334155),
                                            topLeft = Offset(bodyStart, bodyTop),
                                            size = Size(bodyWidth, bodyHeight),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                            style = Stroke(width = 1.dp.toPx())
                                        )

                                        // Draw 4 Bands
                                        val bandW = bodyWidth * 0.11f
                                        val band1X = bodyStart + bodyWidth * 0.14f
                                        val band2X = bodyStart + bodyWidth * 0.35f
                                        val multX = bodyStart + bodyWidth * 0.56f
                                        val tolX = bodyStart + bodyWidth * 0.77f

                                        // Band 1
                                        drawRect(
                                            color = digitBands[band1Idx].color,
                                            topLeft = Offset(band1X, bodyTop),
                                            size = Size(bandW, bodyHeight)
                                        )
                                        // Band 2
                                        drawRect(
                                            color = digitBands[band2Idx].color,
                                            topLeft = Offset(band2X, bodyTop),
                                            size = Size(bandW, bodyHeight)
                                        )
                                        // Multiplier
                                        drawRect(
                                            color = multiplierBands[multIdx].color,
                                            topLeft = Offset(multX, bodyTop),
                                            size = Size(bandW, bodyHeight)
                                        )
                                        // Tolerance
                                        drawRect(
                                            color = toleranceBands[tolIdx].color,
                                            topLeft = Offset(tolX, bodyTop),
                                            size = Size(bandW, bodyHeight)
                                        )
                                    }

                                    // Resistance Readout
                                    Surface(
                                        shape = MicroTagShape,
                                        color = ThemeSurfaceElevated,
                                        border = BorderStroke(0.8.dp, Color(0xFF06B6D4).copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = resistanceFormatted,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF06B6D4),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    // 4 Interactive Color-Dot Pills (Tap to cycle on Home screen)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ColorDotPill(
                                            label = "B1",
                                            color = digitBands[band1Idx].color,
                                            onClick = {
                                                band1Idx = (band1Idx + 1) % digitBands.size
                                                if (UserSettings.hapticFeedback) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        )
                                        ColorDotPill(
                                            label = "B2",
                                            color = digitBands[band2Idx].color,
                                            onClick = {
                                                band2Idx = (band2Idx + 1) % digitBands.size
                                                if (UserSettings.hapticFeedback) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        )
                                        ColorDotPill(
                                            label = "MUL",
                                            color = multiplierBands[multIdx].color,
                                            onClick = {
                                                multIdx = (multIdx + 1) % multiplierBands.size
                                                if (UserSettings.hapticFeedback) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        )
                                        ColorDotPill(
                                            label = "TOL",
                                            color = toleranceBands[tolIdx].color,
                                            onClick = {
                                                tolIdx = (tolIdx + 1) % toleranceBands.size
                                                if (UserSettings.hapticFeedback) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Right Column: Stack of 2 Cards (Weight 1.0f)
                        Column(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Right Top Stacked Card: Board Pinouts (Emerald Accent #10B981)
                            Card(
                                shape = BentoCardShape,
                                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                                border = BorderStroke(
                                    1.dp,
                                    Brush.linearGradient(listOf(Color(0xFF10B981).copy(alpha = 0.5f), ThemeOutline))
                                ),
                                onClick = { onNavigateToFeature("feature_ref/0") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.16f), Color.Transparent),
                                                center = Offset(0f, 0f),
                                                radius = 200f
                                            )
                                        )
                                        .padding(9.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(Color(0xFF10B981), CircleShape)
                                                )
                                                Text(
                                                    text = "BOARD PINOUTS",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                            Text(
                                                text = "Microcontroller Maps",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Surface(
                                                shape = MicroTagShape,
                                                color = ThemeSurfaceElevated,
                                                border = BorderStroke(0.8.dp, ThemeOutline)
                                            ) {
                                                Text(
                                                    text = "ESP32 • STM32 • UNO",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                        }

                                        // Canvas: Dual-Row Microcontroller Chip Graphic
                                        Canvas(
                                            modifier = Modifier
                                                .size(36.dp, 56.dp)
                                                .padding(start = 2.dp)
                                        ) {
                                            val chipW = size.width * 0.6f
                                            val chipH = size.height * 0.85f
                                            val chipLeft = (size.width - chipW) / 2f
                                            val chipTop = (size.height - chipH) / 2f

                                            // Draw IC pins on left and right
                                            val pinCount = 5
                                            val pinSpacing = chipH / (pinCount + 1)
                                            for (i in 1..pinCount) {
                                                val pinY = chipTop + i * pinSpacing
                                                // Left pin
                                                drawLine(
                                                    color = Color(0xFF10B981),
                                                    start = Offset(0f, pinY),
                                                    end = Offset(chipLeft, pinY),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                                // Right pin
                                                drawLine(
                                                    color = Color(0xFF10B981),
                                                    start = Offset(chipLeft + chipW, pinY),
                                                    end = Offset(size.width, pinY),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }

                                            // IC Body
                                            drawRoundRect(
                                                color = Color(0xFF131E2A),
                                                topLeft = Offset(chipLeft, chipTop),
                                                size = Size(chipW, chipH),
                                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                            )
                                            drawRoundRect(
                                                color = Color(0xFF10B981).copy(alpha = 0.6f),
                                                topLeft = Offset(chipLeft, chipTop),
                                                size = Size(chipW, chipH),
                                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                                style = Stroke(width = 1.dp.toPx())
                                            )

                                            // Pin 1 Index Dot
                                            drawCircle(
                                                color = Color(0xFF10B981),
                                                radius = 1.8.dp.toPx(),
                                                center = Offset(chipLeft + 4.dp.toPx(), chipTop + 4.dp.toPx())
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Bottom Stacked Card: 555 & Logic Studio (Indigo / Amber Accent)
                            Card(
                                shape = BentoCardShape,
                                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                                border = BorderStroke(
                                    1.dp,
                                    Brush.linearGradient(listOf(Color(0xFF818CF8).copy(alpha = 0.5f), ThemeOutline))
                                ),
                                onClick = { onNavigateToFeature("feature_ref/1") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val phase by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1800, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "phase"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Color(0xFF818CF8).copy(alpha = 0.16f), Color.Transparent),
                                                center = Offset(0f, 0f),
                                                radius = 200f
                                            )
                                        )
                                        .padding(9.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(Color(0xFF818CF8), CircleShape)
                                                )
                                                Text(
                                                    text = "555 & LOGIC",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF818CF8)
                                                )
                                            }
                                            Text(
                                                text = "Pulse & Gate Studio",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Surface(
                                                shape = MicroTagShape,
                                                color = ThemeSurfaceElevated,
                                                border = BorderStroke(0.8.dp, ThemeOutline)
                                            ) {
                                                Text(
                                                    text = "Pulse • Logic Gates",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF818CF8)
                                                )
                                            }
                                        }

                                        // Canvas: Animated Square Wave + Logic Gate Symbol
                                        Canvas(
                                            modifier = Modifier
                                                .size(46.dp, 40.dp)
                                                .padding(start = 2.dp)
                                        ) {
                                            val w = size.width
                                            val h = size.height
                                            val waveYTop = h * 0.28f
                                            val waveYBottom = h * 0.72f
                                            val period = w * 0.5f

                                            val wavePath = Path()
                                            val offset = phase * period
                                            var currentX = -period + offset

                                            wavePath.moveTo(currentX, waveYBottom)
                                            while (currentX < w + period) {
                                                wavePath.lineTo(currentX + period * 0.5f, waveYBottom)
                                                wavePath.lineTo(currentX + period * 0.5f, waveYTop)
                                                wavePath.lineTo(currentX + period, waveYTop)
                                                wavePath.lineTo(currentX + period, waveYBottom)
                                                currentX += period
                                            }

                                            drawPath(
                                                path = wavePath,
                                                color = Color(0xFF818CF8),
                                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                            )

                                            // Draw glowing pulse dot
                                            drawCircle(
                                                color = Color(0xFFF59E0B),
                                                radius = 2.5.dp.toPx(),
                                                center = Offset(w * 0.5f, if (phase > 0.5f) waveYTop else waveYBottom)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "SEARCH RESULTS (${filteredFeatures.size})" else "ALL TOOLS & MODULES (${filteredFeatures.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // 6. Multi-Accent 2-Column Bento Tool Cards (or 1-Column when toggled)
            if (filteredFeatures.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = BentoCardShape,
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        border = BorderStroke(1.dp, ThemeOutline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No tools match \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                },
                                shape = CategoryPillShape,
                                border = BorderStroke(1.dp, ThemePrimary)
                            ) {
                                Text("Reset Search", color = ThemePrimary)
                            }
                        }
                    }
                }
            } else if (UserSettings.isCompactGridView) {
                // 1-Column Detailed Rich Cards
                items(
                    items = filteredFeatures,
                    key = { it.id },
                    contentType = { "feature_card" }
                ) { feature ->
                    val isPinned = feature.id in UserSettings.pinnedFeatureIds
                    TactileRichFeatureCard(
                        feature = feature,
                        isPinned = isPinned,
                        onLaunch = { onNavigateToFeature(feature.route) },
                        onTogglePin = {
                            UserSettings.togglePinnedFeature(feature.id, context)
                            if (UserSettings.hapticFeedback) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)
                    )
                }
            } else {
                // Default: Multi-Accent 2-Column Bento Tool Cards
                val chunked = filteredFeatures.chunked(2)
                items(
                    items = chunked,
                    key = { row -> row.joinToString("-") { it.id } }
                ) { rowFeatures ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowFeatures.forEach { feature ->
                            val isPinned = feature.id in UserSettings.pinnedFeatureIds
                            TactileBentoToolCard(
                                feature = feature,
                                isPinned = isPinned,
                                onLaunch = { onNavigateToFeature(feature.route) },
                                onTogglePin = {
                                    UserSettings.togglePinnedFeature(feature.id, context)
                                    if (UserSettings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowFeatures.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 7. Subtle Theme Picker Link at Bottom
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ThemeSurface,
                        border = BorderStroke(1.dp, ThemeOutline),
                        modifier = Modifier
                            .clickable { onOpenThemeDialog() }
                            .testTag("home_bottom_theme_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Palette,
                                contentDescription = "Theme",
                                tint = ThemePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Theme: ${ThemeManager.currentTheme.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Change",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ThemePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clickable color dot pill in Left Hero Resistor Decoder.
 */
@Composable
private fun ColorDotPill(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = ThemeSurfaceElevated,
        border = BorderStroke(0.8.dp, ThemeOutline),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(color, CircleShape)
                    .border(0.6.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Multi-Accent 2-Column Bento Tool Card with Watermark Blueprint Graphic & Vertical Accent Indicator Pill.
 */
@Composable
private fun TactileBentoToolCard(
    feature: AppFeatureItem,
    isPinned: Boolean,
    onLaunch: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            feature.accentColor.copy(alpha = 0.45f),
            ThemeOutline
        ),
        start = Offset(0f, 0f),
        end = Offset(300f, 300f)
    )

    Card(
        onClick = onLaunch,
        modifier = modifier
            .testTag("feature_card_${feature.id}"),
        shape = BentoCardShape,
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        border = BorderStroke(1.dp, borderBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
        ) {
            // Top-right corner subtle radial glow
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(feature.accentColor.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width, 0f)
                )
            }

            // Bottom-right watermark Canvas drawing symbolic mini graphic for the tool
            Canvas(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
            ) {
                drawFeatureSymbolWatermark(feature.id, feature.accentColor)
            }

            // Left 3.dp vertical glowing accent indicator pill
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(feature.accentColor, feature.accentColor.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Icon box + Quick pin icon button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(feature.accentColor.copy(alpha = 0.16f), IconBoxShape)
                            .border(1.dp, feature.accentColor.copy(alpha = 0.35f), IconBoxShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            feature.icon,
                            contentDescription = feature.title,
                            tint = feature.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = if (isPinned) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Middle: Title & Subtitle
                Column {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = feature.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom: Monospace micro-tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MicroTagShape,
                        color = ThemeSurfaceElevated,
                        border = BorderStroke(0.8.dp, ThemeOutline)
                    ) {
                        Text(
                            text = feature.badges.firstOrNull() ?: feature.category,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = feature.accentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1-Column Tactile Rich Feature Card with Watermark Blueprint Graphic & Vertical Accent Indicator Pill.
 */
@Composable
private fun TactileRichFeatureCard(
    feature: AppFeatureItem,
    isPinned: Boolean,
    onLaunch: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderBrush = Brush.linearGradient(
        listOf(
            feature.accentColor.copy(alpha = 0.4f),
            ThemeOutline
        )
    )

    Card(
        onClick = onLaunch,
        modifier = modifier
            .fillMaxWidth()
            .testTag("feature_card_${feature.id}"),
        shape = BentoCardShape,
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        border = BorderStroke(1.dp, borderBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            // Top-right corner subtle radial glow
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(feature.accentColor.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width, 0f)
                )
            }

            // Bottom-right watermark Canvas
            Canvas(
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 4.dp)
            ) {
                drawFeatureSymbolWatermark(feature.id, feature.accentColor)
            }

            // Left 3.dp vertical glowing accent indicator pill
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(feature.accentColor, feature.accentColor.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            // Content row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(feature.accentColor.copy(alpha = 0.16f), IconBoxShape)
                        .border(1.dp, feature.accentColor.copy(alpha = 0.35f), IconBoxShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        feature.icon,
                        contentDescription = feature.title,
                        tint = feature.accentColor,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = feature.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MicroTagShape,
                            color = ThemeSurfaceElevated,
                            border = BorderStroke(0.8.dp, ThemeOutline)
                        ) {
                            Text(
                                text = feature.category,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        feature.badges.take(1).forEach { badge ->
                            Surface(
                                shape = MicroTagShape,
                                color = feature.accentColor.copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, feature.accentColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = badge,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = feature.accentColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) "Unpin" else "Pin",
                        tint = if (isPinned) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Draws symbolic engineering watermark graphic for each tool in the bottom right corner of cards.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFeatureSymbolWatermark(featureId: String, accentColor: Color) {
    val watermarkColor = accentColor.copy(alpha = 0.14f)
    val strokeWidth = 1.5f.dp.toPx()
    val w = size.width
    val h = size.height

    when (featureId) {
        "capacitor_code" -> {
            // Parallel plates with lead wires
            val plateTop = h * 0.25f
            val plateBottom = h * 0.75f
            val plate1X = w * 0.42f
            val plate2X = w * 0.58f

            // Left lead wire
            drawLine(watermarkColor, Offset(w * 0.15f, h * 0.5f), Offset(plate1X, h * 0.5f), strokeWidth)
            // Left plate
            drawLine(watermarkColor, Offset(plate1X, plateTop), Offset(plate1X, plateBottom), strokeWidth * 2f, StrokeCap.Round)
            // Right plate
            drawLine(watermarkColor, Offset(plate2X, plateTop), Offset(plate2X, plateBottom), strokeWidth * 2f, StrokeCap.Round)
            // Right lead wire
            drawLine(watermarkColor, Offset(plate2X, h * 0.5f), Offset(w * 0.85f, h * 0.5f), strokeWidth)
        }
        "ohms_law" -> {
            // Speedometer / Gauge dial arc with needle
            val center = Offset(w * 0.5f, h * 0.7f)
            val radius = w * 0.35f
            drawArc(
                color = watermarkColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            // Needle pointing at ~45°
            drawLine(
                color = watermarkColor,
                start = center,
                end = Offset(center.x + radius * 0.7f * cos(Math.toRadians(45.0)).toFloat(), center.y - radius * 0.7f * sin(Math.toRadians(45.0)).toFloat()),
                strokeWidth = strokeWidth * 1.5f,
                cap = StrokeCap.Round
            )
        }
        "smd_tester" -> {
            // Rectangular chip package with 2 end-caps
            val chipLeft = w * 0.2f
            val chipTop = h * 0.3f
            val chipW = w * 0.6f
            val chipH = h * 0.4f
            drawRoundRect(
                color = watermarkColor,
                topLeft = Offset(chipLeft, chipTop),
                size = Size(chipW, chipH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
            // End-cap pads
            drawRect(watermarkColor, Offset(chipLeft, chipTop), Size(chipW * 0.22f, chipH))
            drawRect(watermarkColor, Offset(chipLeft + chipW * 0.78f, chipTop), Size(chipW * 0.22f, chipH))
        }
        "led_resistor" -> {
            // Diode symbol: triangle + vertical bar + 2 light arrows
            val centerX = w * 0.45f
            val centerY = h * 0.5f
            val triPath = Path().apply {
                moveTo(centerX - 10.dp.toPx(), centerY - 10.dp.toPx())
                lineTo(centerX - 10.dp.toPx(), centerY + 10.dp.toPx())
                lineTo(centerX + 6.dp.toPx(), centerY)
                close()
            }
            drawPath(triPath, watermarkColor, style = Stroke(width = strokeWidth))
            // Bar
            drawLine(watermarkColor, Offset(centerX + 6.dp.toPx(), centerY - 10.dp.toPx()), Offset(centerX + 6.dp.toPx(), centerY + 10.dp.toPx()), strokeWidth * 1.5f)
            // LED Light emission arrows
            drawLine(watermarkColor, Offset(centerX + 2.dp.toPx(), centerY - 8.dp.toPx()), Offset(centerX + 12.dp.toPx(), centerY - 16.dp.toPx()), strokeWidth)
            drawLine(watermarkColor, Offset(centerX + 8.dp.toPx(), centerY - 4.dp.toPx()), Offset(centerX + 18.dp.toPx(), centerY - 12.dp.toPx()), strokeWidth)
        }
        "timer_555" -> {
            // Square wave pulses
            val waveYTop = h * 0.35f
            val waveYBottom = h * 0.65f
            val path = Path().apply {
                moveTo(w * 0.15f, waveYBottom)
                lineTo(w * 0.35f, waveYBottom)
                lineTo(w * 0.35f, waveYTop)
                lineTo(w * 0.65f, waveYTop)
                lineTo(w * 0.65f, waveYBottom)
                lineTo(w * 0.85f, waveYBottom)
            }
            drawPath(path, watermarkColor, style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round))
        }
        "resistor_color" -> {
            // Resistor outline with stripes
            val bodyLeft = w * 0.22f
            val bodyTop = h * 0.35f
            val bodyW = w * 0.56f
            val bodyH = h * 0.3f
            drawLine(watermarkColor, Offset(w * 0.1f, h * 0.5f), Offset(w * 0.9f, h * 0.5f), strokeWidth)
            drawRoundRect(watermarkColor, Offset(bodyLeft, bodyTop), Size(bodyW, bodyH), CornerRadius(3.dp.toPx(), 3.dp.toPx()), style = Stroke(width = strokeWidth))
            drawLine(watermarkColor, Offset(bodyLeft + bodyW * 0.3f, bodyTop), Offset(bodyLeft + bodyW * 0.3f, bodyTop + bodyH), strokeWidth)
            drawLine(watermarkColor, Offset(bodyLeft + bodyW * 0.5f, bodyTop), Offset(bodyLeft + bodyW * 0.5f, bodyTop + bodyH), strokeWidth)
            drawLine(watermarkColor, Offset(bodyLeft + bodyW * 0.7f, bodyTop), Offset(bodyLeft + bodyW * 0.7f, bodyTop + bodyH), strokeWidth)
        }
        "microcontroller_pinouts" -> {
            // IC package outline with pins
            val icW = w * 0.5f
            val icH = h * 0.5f
            val icLeft = (w - icW) / 2f
            val icTop = (h - icH) / 2f
            drawRoundRect(watermarkColor, Offset(icLeft, icTop), Size(icW, icH), CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = strokeWidth))
            for (i in 1..3) {
                val py = icTop + i * (icH / 4)
                drawLine(watermarkColor, Offset(icLeft - 4.dp.toPx(), py), Offset(icLeft, py), strokeWidth)
                drawLine(watermarkColor, Offset(icLeft + icW, py), Offset(icLeft + icW + 4.dp.toPx(), py), strokeWidth)
            }
        }
        "logic_simulator" -> {
            // AND Gate D-shape
            val gateLeft = w * 0.3f
            val gateTop = h * 0.3f
            val gateW = w * 0.4f
            val gateH = h * 0.4f
            val gatePath = Path().apply {
                moveTo(gateLeft, gateTop)
                lineTo(gateLeft + gateW * 0.5f, gateTop)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(gateLeft, gateTop, gateLeft + gateW, gateTop + gateH),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                lineTo(gateLeft, gateTop + gateH)
                close()
            }
            drawPath(gatePath, watermarkColor, style = Stroke(width = strokeWidth))
        }
        else -> {
            // Concentric blueprint tech circles
            drawCircle(watermarkColor, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = strokeWidth))
            drawCircle(watermarkColor, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = strokeWidth * 0.75f))
        }
    }
}

/**
 * Formats resistor value to readable string, e.g. 10.0 kΩ ±5%.
 */
private fun formatResistorValue(rawOhms: Double, tolerancePct: Int): String {
    val formatted = when {
        rawOhms >= 1_000_000.0 -> {
            val m = rawOhms / 1_000_000.0
            if (m == m.toLong().toDouble()) "${m.toLong()} MΩ" else String.format(Locale.US, "%.1f MΩ", m)
        }
        rawOhms >= 1_000.0 -> {
            val k = rawOhms / 1_000.0
            if (k == k.toLong().toDouble()) "${k.toLong()}.0 kΩ" else String.format(Locale.US, "%.1f kΩ", k)
        }
        else -> {
            if (rawOhms == rawOhms.toLong().toDouble()) "${rawOhms.toLong()} Ω" else String.format(Locale.US, "%.1f Ω", rawOhms)
        }
    }
    return "$formatted ±$tolerancePct%"
}
