package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

// =================================================================
// HELPER FORMATTING & HAPTIC UTILITIES
// =================================================================

fun formatResistance(ohms: Double, precision: Int = UserSettings.decimalPrecision): String {
    val p = precision.coerceIn(1, 6)
    val fmt = "%.${p}f"
    return when {
        ohms >= 1_000_000_000 -> String.format(Locale.US, "$fmt GΩ", ohms / 1_000_000_000)
        ohms >= 1_000_000 -> String.format(Locale.US, "$fmt MΩ", ohms / 1_000_000)
        ohms >= 1_000 -> String.format(Locale.US, "$fmt kΩ", ohms / 1_000)
        else -> String.format(Locale.US, "$fmt Ω", ohms)
    }
}

fun formatDouble(value: Double, precision: Int = UserSettings.decimalPrecision): String {
    val p = precision.coerceIn(1, 6)
    return String.format(Locale.US, "%.${p}f", value)
}

@Composable
fun CalculatorFormulaCard(title: String, formula: String, note: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceElevated),
        border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Functions,
                    contentDescription = null,
                    tint = ThemePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ThemeSurface,
                border = BorderStroke(1.dp, ThemeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formula,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =================================================================
// MAIN CALCULATOR SCREEN WITH ANIMATED TAB CONTENT
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    initialToolIndex: Int = 0,
    onToolChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTool by remember(initialToolIndex) { mutableIntStateOf(initialToolIndex) }
    val haptic = LocalHapticFeedback.current

    val tools = listOf(
        "Resistor Colors" to Icons.Outlined.Palette,
        "SMD Tester" to Icons.Outlined.Memory,
        "Capacitor Code" to Icons.Outlined.ElectricBolt,
        "555 Timer Studio" to Icons.Outlined.Timer,
        "LED Designer" to Icons.Outlined.Lightbulb,
        "Ohm's Law" to Icons.Outlined.Speed
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        // Tool Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTool,
            edgePadding = 16.dp,
            containerColor = ThemeSurface,
            contentColor = ThemePrimary,
            divider = {
                HorizontalDivider(color = ThemeOutline)
            }
        ) {
            tools.forEachIndexed { index, (title, icon) ->
                val isSelected = selectedTool == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        if (UserSettings.hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        selectedTool = index
                        onToolChange(index)
                    },
                    modifier = Modifier.testTag("calc_tab_$index"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        // Smooth horizontal slide + fade transition when changing tabs
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            AnimatedContent(
                targetState = selectedTool,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(animationSpec = tween(300)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut(animationSpec = tween(300)))
                    }
                },
                label = "calc_tab_animated_content"
            ) { toolIndex ->
                when (toolIndex) {
                    0 -> ResistorColorCodeTool()
                    1 -> SmdResistorTesterTool()
                    2 -> CapacitorCodeDecoderTool()
                    3 -> Timer555StudioTool()
                    4 -> LedResistorDesignerTool()
                    5 -> OhmsLawStudioTool()
                    else -> ResistorColorCodeTool()
                }
            }
        }
    }
}

// =================================================================
// 1. ADVANCED RESISTOR COLOR CODE CALCULATOR WITH LIVE CANVAS
// =================================================================

data class ColorBand(val name: String, val color: Color, val digit: Int, val multiplier: Double, val tolerance: Double?)

val standardBands = listOf(
    ColorBand("Black", Color(0xFF1E232A), 0, 1.0, null),
    ColorBand("Brown", Color(0xFF8B4513), 1, 10.0, 1.0),
    ColorBand("Red", Color(0xFFDC2626), 2, 100.0, 2.0),
    ColorBand("Orange", Color(0xFFEA580C), 3, 1_000.0, 0.05),
    ColorBand("Yellow", Color(0xFFCA8A04), 4, 10_000.0, 0.02),
    ColorBand("Green", Color(0xFF16A34A), 5, 100_000.0, 0.5),
    ColorBand("Blue", Color(0xFF2563EB), 6, 1_000_000.0, 0.25),
    ColorBand("Violet", Color(0xFF9333EA), 7, 10_000_000.0, 0.1),
    ColorBand("Gray", Color(0xFF64748B), 8, 100_000_000.0, 0.01),
    ColorBand("White", Color(0xFFF1F5F9), 9, 1_000_000_000.0, null),
    ColorBand("Gold", Color(0xFFD97706), -1, 0.1, 5.0),
    ColorBand("Silver", Color(0xFF94A3B8), -2, 0.01, 10.0)
)

data class TempCoeffBand(val name: String, val color: Color, val ppm: Int)

val standardTempCoeffBands = listOf(
    TempCoeffBand("Brown", Color(0xFF8B4513), 100),
    TempCoeffBand("Red", Color(0xFFDC2626), 50),
    TempCoeffBand("Orange", Color(0xFFEA580C), 15),
    TempCoeffBand("Yellow", Color(0xFFCA8A04), 25),
    TempCoeffBand("Blue", Color(0xFF2563EB), 10),
    TempCoeffBand("Violet", Color(0xFF9333EA), 5)
)

@Composable
fun ResistorColorCodeTool() {
    val haptic = LocalHapticFeedback.current

    // Initialize tolerance based on UserSettings.defaultResistorTolerance
    val initialTolIdx = remember {
        val def = UserSettings.defaultResistorTolerance
        val matched = standardBands.indexOfFirst { it.tolerance != null && def.contains(it.name, ignoreCase = true) }
        if (matched >= 0) matched else 10 // Default Gold (±5%)
    }

    var bandCount by remember { mutableIntStateOf(4) }
    var band1Idx by remember { mutableIntStateOf(1) } // Brown (1)
    var band2Idx by remember { mutableIntStateOf(0) } // Black (0)
    var band3Idx by remember { mutableIntStateOf(2) } // Red (2)
    var multiplierIdx by remember { mutableIntStateOf(2) } // Red (x100)
    var toleranceIdx by remember { mutableIntStateOf(initialTolIdx) }
    var tempCoeffIdx by remember { mutableIntStateOf(0) } // Brown (100 PPM/K)

    val scrollState = rememberScrollState()

    fun triggerHaptic() {
        if (UserSettings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val (resistanceValue, formattedValue, toleranceStr, tempCoeffStr) = remember(
        bandCount, band1Idx, band2Idx, band3Idx, multiplierIdx, toleranceIdx, tempCoeffIdx, UserSettings.decimalPrecision
    ) {
        val b1 = standardBands[band1Idx].digit.coerceAtLeast(0)
        val b2 = standardBands[band2Idx].digit.coerceAtLeast(0)
        val b3 = standardBands[band3Idx].digit.coerceAtLeast(0)
        val mult = standardBands[multiplierIdx].multiplier

        val rawOhms = if (bandCount == 4) {
            (b1 * 10 + b2) * mult
        } else {
            (b1 * 100 + b2 * 10 + b3) * mult
        }

        val tol = standardBands[toleranceIdx].tolerance ?: 5.0
        val formatted = formatResistance(rawOhms, UserSettings.decimalPrecision)
        val tcr = if (bandCount == 6) "${standardTempCoeffBands[tempCoeffIdx].ppm} PPM/K" else ""
        listOf(rawOhms.toString(), formatted, "±$tol%", tcr)
    }

    // Smoothly morph color bands on the canvas using animateColorAsState(tween(350))
    val animatedColor1 by animateColorAsState(
        targetValue = standardBands[band1Idx].color,
        animationSpec = tween(350),
        label = "anim_band1"
    )
    val animatedColor2 by animateColorAsState(
        targetValue = standardBands[band2Idx].color,
        animationSpec = tween(350),
        label = "anim_band2"
    )
    val animatedColor3 by animateColorAsState(
        targetValue = standardBands[band3Idx].color,
        animationSpec = tween(350),
        label = "anim_band3"
    )
    val animatedMultiplierColor by animateColorAsState(
        targetValue = standardBands[multiplierIdx].color,
        animationSpec = tween(350),
        label = "anim_multiplier"
    )
    val animatedToleranceColor by animateColorAsState(
        targetValue = standardBands[toleranceIdx].color,
        animationSpec = tween(350),
        label = "anim_tolerance"
    )
    val animatedTempColor by animateColorAsState(
        targetValue = standardTempCoeffBands[tempCoeffIdx].color,
        animationSpec = tween(350),
        label = "anim_temp"
    )

    val activeColors = remember(bandCount, animatedColor1, animatedColor2, animatedColor3, animatedMultiplierColor, animatedToleranceColor, animatedTempColor) {
        when (bandCount) {
            4 -> listOf(animatedColor1, animatedColor2, animatedMultiplierColor, animatedToleranceColor)
            5 -> listOf(animatedColor1, animatedColor2, animatedColor3, animatedMultiplierColor, animatedToleranceColor)
            else -> listOf(animatedColor1, animatedColor2, animatedColor3, animatedMultiplierColor, animatedToleranceColor, animatedTempColor)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Band Count Selector (4, 5, 6 bands)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ThemeSurface,
                border = BorderStroke(1.dp, ThemeOutline)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf(4, 5, 6).forEach { count ->
                        val isSelected = bandCount == count
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ThemePrimary else Color.Transparent,
                            modifier = Modifier
                                .clickable {
                                    triggerHaptic()
                                    bandCount = count
                                }
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "$count BANDS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REALISTIC AXIAL RESISTOR CANVAS DRAWING
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
            border = BorderStroke(1.dp, ThemeOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE RESISTOR VISUALIZATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemePrimary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "$bandCount BANDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Canvas Resistor Body with smoothly animated color bands
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f

                    // Metal Leads (Left & Right)
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(0f, centerY),
                        end = Offset(w, centerY),
                        strokeWidth = 10f
                    )

                    // Ceramic Resistor Body Center Dimensions
                    val bodyWidth = (w * 0.70f).coerceAtMost(320.dp.toPx())
                    val bodyHeight = 46.dp.toPx()
                    val bodyLeft = (w - bodyWidth) / 2f
                    val bodyTop = centerY - bodyHeight / 2f

                    // Shaded Resistor Ceramic Body
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFE2D6B5), Color(0xFFD4C29A), Color(0xFFBAA478))
                        ),
                        topLeft = Offset(bodyLeft, bodyTop),
                        size = Size(bodyWidth, bodyHeight),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    // Bulbous Ends
                    val bulbW = 22.dp.toPx()
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFEDE2C8), Color(0xFFC9B68C))
                        ),
                        topLeft = Offset(bodyLeft, bodyTop - 3.dp.toPx()),
                        size = Size(bulbW, bodyHeight + 6.dp.toPx()),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFEDE2C8), Color(0xFFC9B68C))
                        ),
                        topLeft = Offset(bodyLeft + bodyWidth - bulbW, bodyTop - 3.dp.toPx()),
                        size = Size(bulbW, bodyHeight + 6.dp.toPx()),
                        cornerRadius = CornerRadius(14f, 14f)
                    )

                    // Draw Color Bands
                    val bandWidth = if (bandCount == 6) 9.dp.toPx() else 11.dp.toPx()
                    val numBands = activeColors.size
                    val spacing = (bodyWidth - (bulbW * 1.5f) - (numBands * bandWidth)) / numBands.coerceAtLeast(1)

                    activeColors.forEachIndexed { i, col ->
                        val bandX = bodyLeft + bulbW + (i * (bandWidth + spacing))
                        drawRect(
                            color = col,
                            topLeft = Offset(bandX, bodyTop - 2.dp.toPx()),
                            size = Size(bandWidth, bodyHeight + 4.dp.toPx())
                        )
                        // Band shadow/highlight
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(bandX + 2f, bodyTop),
                            end = Offset(bandX + 2f, bodyTop + bodyHeight),
                            strokeWidth = 2f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Value Display Box with AnimatedContent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RESISTANCE READOUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AnimatedContent(
                            targetState = formattedValue,
                            transitionSpec = {
                                (slideInVertically(animationSpec = tween(220)) { height -> height / 2 } + fadeIn(animationSpec = tween(220)))
                                    .togetherWith(slideOutVertically(animationSpec = tween(220)) { height -> -height / 2 } + fadeOut(animationSpec = tween(220)))
                            },
                            label = "resistance_readout_anim"
                        ) { targetVal ->
                            Text(
                                text = targetVal,
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ThemePrimary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ThemePrimaryContainer,
                            border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "TOLERANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.5.sp,
                                    color = ThemePrimary
                                )
                                Text(
                                    text = toleranceStr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (bandCount == 6) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, ThemeAccentAmber.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "TCR (PPM)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.5.sp,
                                        color = ThemeAccentAmber
                                    )
                                    Text(
                                        text = tempCoeffStr,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeAccentAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BAND SELECTION PALETTES
        BandSelectorRow("1st Digit Band", standardBands.take(10), band1Idx) {
            triggerHaptic()
            band1Idx = it
        }
        BandSelectorRow("2nd Digit Band", standardBands.take(10), band2Idx) {
            triggerHaptic()
            band2Idx = it
        }
        if (bandCount >= 5) {
            BandSelectorRow("3rd Digit Band", standardBands.take(10), band3Idx) {
                triggerHaptic()
                band3Idx = it
            }
        }
        BandSelectorRow("Multiplier Band", standardBands, multiplierIdx) {
            triggerHaptic()
            multiplierIdx = it
        }
        BandSelectorRow(
            "Tolerance Band",
            standardBands.filter { it.tolerance != null },
            standardBands.filter { it.tolerance != null }.indexOfFirst { it.name == standardBands[toleranceIdx].name }.coerceAtLeast(0)
        ) { filteredIdx ->
            triggerHaptic()
            val actual = standardBands.filter { it.tolerance != null }[filteredIdx]
            toleranceIdx = standardBands.indexOf(actual)
        }

        // 6th Band: Temperature Coefficient (PPM/K) Selector Row
        if (bandCount == 6) {
            TempCoeffSelectorRow("6th Band: Temp Coefficient (TCR)", standardTempCoeffBands, tempCoeffIdx) {
                triggerHaptic()
                tempCoeffIdx = it
            }
        }

        // Formula Cheatsheet if enabled in UserSettings
        if (UserSettings.showFormulasAlways) {
            Spacer(modifier = Modifier.height(10.dp))
            CalculatorFormulaCard(
                title = "Resistor Code Formula",
                formula = if (bandCount == 4) "R = (D1 × 10 + D2) × 10^Multiplier (±Tol%)" else "R = (D1 × 100 + D2 × 10 + D3) × 10^Multiplier (±Tol%)",
                note = if (bandCount == 6) "6-Band adds Temperature Coefficient: TCR = ${standardTempCoeffBands[tempCoeffIdx].ppm} PPM/K." else "Color bands represent significant digits, decimal multiplier, and tolerance."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BandSelectorRow(
    title: String,
    bands: List<ColorBand>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selectedIdx in bands.indices) {
                Text(
                    text = bands[selectedIdx].name,
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bands.size) { index ->
                val band = bands[index]
                val isSelected = index == selectedIdx
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) ThemePrimary.copy(alpha = 0.2f) else ThemeSurface,
                    border = BorderStroke(1.5.dp, if (isSelected) ThemePrimary else ThemeOutline),
                    modifier = Modifier.clickable { onSelect(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(band.color)
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                        )
                        Text(
                            text = if (band.digit >= 0) "${band.digit}" else band.name.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TempCoeffSelectorRow(
    title: String,
    bands: List<TempCoeffBand>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ThemeAccentAmber
            )
            if (selectedIdx in bands.indices) {
                Text(
                    text = "${bands[selectedIdx].name} (${bands[selectedIdx].ppm} PPM/K)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemeAccentAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bands.size) { index ->
                val band = bands[index]
                val isSelected = index == selectedIdx
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) ThemeAccentAmber.copy(alpha = 0.2f) else ThemeSurface,
                    border = BorderStroke(1.5.dp, if (isSelected) ThemeAccentAmber else ThemeOutline),
                    modifier = Modifier.clickable { onSelect(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(band.color)
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                        )
                        Text(
                            text = "${band.ppm} PPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) ThemeAccentAmber else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// =================================================================
// 2. SMD CODE TESTER & PRECISION DECODER
// =================================================================

@Composable
fun SmdResistorTesterTool() {
    val haptic = LocalHapticFeedback.current
    var codeInput by remember { mutableStateOf("103") }
    val scrollState = rememberScrollState()

    val quickPresets = listOf("103", "472", "1001", "4R7", "R050", "01C", "68B")

    val result = remember(codeInput, UserSettings.decimalPrecision) {
        decodeAdvancedSmd(codeInput.trim().uppercase())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "SMD RESISTOR CODE ANALYZER",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Decodes 3-Digit (5%), 4-Digit (1%), R-Decimal notation, and EIA-96 precision codes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Preset chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPresets) { preset ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (codeInput == preset) ThemePrimary.copy(alpha = 0.2f) else ThemeSurface,
                    border = BorderStroke(1.dp, if (codeInput == preset) ThemePrimary else ThemeOutline),
                    modifier = Modifier.clickable {
                        if (UserSettings.hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        codeInput = preset
                    }
                ) {
                    Text(
                        text = preset,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (codeInput == preset) ThemePrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("Enter Marking Code (e.g., 472, 1002, 4R7, 01C)") },
            placeholder = { Text("Code") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("smd_code_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ThemeSurface,
                unfocusedContainerColor = ThemeSurface,
                focusedBorderColor = ThemePrimary,
                unfocusedBorderColor = ThemeOutline
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Realistic SMD Chip Graphic Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
            border = BorderStroke(1.dp, ThemeOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visual SMD Package Graphic
                Row(
                    modifier = Modifier
                        .size(140.dp, 64.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ThemeSurfaceElevated)
                        .border(1.dp, ThemeOutlineBright, RoundedCornerShape(4.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Solder Terminal
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(18.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))))
                    )
                    // Black Ceramic Resistor Body with Laser Marking
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF111827)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = codeInput.ifEmpty { "SMD" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                    // Right Solder Terminal
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(18.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1))))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DECODED RESISTANCE VALUE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.formattedValue,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = ThemePrimary
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ThemeOutline)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("STANDARD FORMAT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(result.formatType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOLERANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(result.tolerance, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ThemeAccentGreen)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        if (UserSettings.showFormulasAlways) {
            Spacer(modifier = Modifier.height(10.dp))
            CalculatorFormulaCard(
                title = "SMD Decoding Formula",
                formula = "3-Digit: AB × 10^C Ω | 4-Digit: ABC × 10^D Ω | 'R' = Decimal Point",
                note = "EIA-96: 2-digit code gives significant figure; letter suffix determines decade multiplier."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

data class AdvancedSmdResult(
    val formattedValue: String,
    val formatType: String,
    val tolerance: String,
    val explanation: String
)

fun decodeAdvancedSmd(code: String): AdvancedSmdResult {
    if (code.isBlank()) {
        return AdvancedSmdResult("—", "None", "—", "Enter a code above")
    }

    if (code.contains("R")) {
        val valDouble = code.replace("R", ".").toDoubleOrNull()
        if (valDouble != null) {
            return AdvancedSmdResult(
                formattedValue = formatResistance(valDouble),
                formatType = "Decimal 'R' System",
                tolerance = "±1% to ±5%",
                explanation = "The letter 'R' acts as the decimal point ($code = $valDouble Ohms)."
            )
        }
    }

    if (code.length == 3 && code.all { it.isDigit() }) {
        val d1 = code[0].digitToInt()
        val d2 = code[1].digitToInt()
        val exp = code[2].digitToInt()
        val ohms = (d1 * 10 + d2) * 10.0.pow(exp)
        return AdvancedSmdResult(
            formattedValue = formatResistance(ohms),
            formatType = "3-Digit Standard",
            tolerance = "±5%",
            explanation = "Digits $d1 and $d2 are significant figures, followed by $exp zeros ($d1$d2 × 10^$exp = $ohms Ω)."
        )
    }

    if (code.length == 4 && code.all { it.isDigit() }) {
        val d1 = code[0].digitToInt()
        val d2 = code[1].digitToInt()
        val d3 = code[2].digitToInt()
        val exp = code[3].digitToInt()
        val ohms = (d1 * 100 + d2 * 10 + d3) * 10.0.pow(exp)
        return AdvancedSmdResult(
            formattedValue = formatResistance(ohms),
            formatType = "4-Digit Precision",
            tolerance = "±1%",
            explanation = "Digits $d1$d2$d3 are significant figures, followed by $exp zeros ($d1$d2$d3 × 10^$exp = $ohms Ω)."
        )
    }

    val eia96Multipliers = mapOf(
        'Z' to 0.001, 'Y' to 0.01, 'R' to 0.01, 'X' to 0.1, 'S' to 0.1,
        'A' to 1.0, 'B' to 10.0, 'C' to 100.0, 'D' to 1_000.0, 'E' to 10_000.0, 'F' to 100_000.0
    )
    if (code.length == 3 && code.take(2).all { it.isDigit() } && code.last() in eia96Multipliers) {
        val codeNum = code.take(2).toInt()
        val multChar = code.last()
        val mult = eia96Multipliers[multChar] ?: 1.0
        val baseVal = when (codeNum) {
            1 -> 100; 2 -> 102; 3 -> 105; 4 -> 107; 5 -> 110
            68 -> 499; 85 -> 750; else -> 100 + (codeNum - 1) * 5
        }
        val ohms = baseVal * mult
        return AdvancedSmdResult(
            formattedValue = formatResistance(ohms),
            formatType = "EIA-96 Precision",
            tolerance = "±1%",
            explanation = "EIA-96 standard lookup: Code $codeNum represents $baseVal with multiplier '$multChar' (×$mult)."
        )
    }

    return AdvancedSmdResult("Invalid Format", "Unknown", "—", "Enter a 3-digit, 4-digit, or EIA-96 code (e.g. 103, 4702, 01C, 4R7).")
}

// =================================================================
// 3. CAPACITOR VALUE DECODER
// =================================================================

@Composable
fun CapacitorCodeDecoderTool() {
    val haptic = LocalHapticFeedback.current
    var codeInput by remember { mutableStateOf("104") }
    var toleranceCode by remember { mutableStateOf("K") }
    val scrollState = rememberScrollState()

    val presets = listOf("102", "103", "104", "224", "473", "105")

    val result = remember(codeInput, toleranceCode, UserSettings.decimalPrecision) {
        decodeCapacitor(codeInput.trim(), toleranceCode)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "CAPACITOR CODE & DIELECTRIC DECODER",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Decodes 3-digit ceramic and film capacitor markings into pF, nF, and µF.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { preset ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (codeInput == preset) ThemePrimary.copy(alpha = 0.2f) else ThemeSurface,
                    border = BorderStroke(1.dp, if (codeInput == preset) ThemePrimary else ThemeOutline),
                    modifier = Modifier.clickable {
                        if (UserSettings.hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        codeInput = preset
                    }
                ) {
                    Text(
                        text = preset,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (codeInput == preset) ThemePrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                label = { Text("Marking Code") },
                placeholder = { Text("e.g. 104") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("cap_code_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Tolerance selector
            val tolerances = listOf("J (±5%)", "K (±10%)", "M (±20%)")
            var expandedTol by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(0.8f)) {
                OutlinedTextField(
                    value = toleranceCode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tolerance") },
                    trailingIcon = {
                        IconButton(onClick = { expandedTol = !expandedTol }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedTol = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = expandedTol,
                    onDismissRequest = { expandedTol = false }
                ) {
                    tolerances.forEach { tol ->
                        DropdownMenuItem(
                            text = { Text(tol) },
                            onClick = {
                                if (UserSettings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                toleranceCode = tol.take(1)
                                expandedTol = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ceramic Capacitor Visual Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
            border = BorderStroke(1.dp, ThemeOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD97706))
                        .border(2.dp, Color(0xFFF59E0B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$codeInput$toleranceCode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "50V",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PRIMARY CAPACITANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.primaryFormatted,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = ThemePrimary
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ThemeOutline)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricTile("Picofarads", result.pF)
                    MetricTile("Nanofarads", result.nF)
                    MetricTile("Microfarads", result.uF)
                }
            }
        }

        if (UserSettings.showFormulasAlways) {
            Spacer(modifier = Modifier.height(10.dp))
            CalculatorFormulaCard(
                title = "Capacitor Code Formula",
                formula = "Capacitance (pF) = Digits[1..2] × 10^Digit[3]",
                note = "1 µF = 1,000 nF = 1,000,000 pF. Tolerances: J (±5%), K (±10%), M (±20%)."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricTile(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ThemeSurfaceElevated,
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

data class CapResult(val pF: String, val nF: String, val uF: String, val primaryFormatted: String)

fun decodeCapacitor(code: String, tol: String): CapResult {
    if (code.length != 3 || !code.all { it.isDigit() }) {
        return CapResult("—", "—", "—", "Enter 3 Digits")
    }

    val d1 = code[0].digitToInt()
    val d2 = code[1].digitToInt()
    val mult = code[2].digitToInt()

    val pFValue = (d1 * 10 + d2) * 10.0.pow(mult)
    val nFValue = pFValue / 1_000.0
    val uFValue = pFValue / 1_000_000.0

    val primary = when {
        uFValue >= 1.0 -> "${formatDouble(uFValue)} µF"
        nFValue >= 1.0 -> "${formatDouble(nFValue)} nF"
        else -> "${formatDouble(pFValue, 0)} pF"
    }

    return CapResult(
        pF = "${pFValue.toLong()} pF",
        nF = "${formatDouble(nFValue)} nF",
        uF = "${formatDouble(uFValue, UserSettings.decimalPrecision.coerceAtLeast(3))} µF",
        primaryFormatted = primary
    )
}

// =================================================================
// 4. 555 TIMER STUDIO & LIVE WAVEFORM SIMULATOR
// =================================================================

enum class Timer555Mode(val title: String, val subtitle: String) {
    ASTABLE("Astable", "Oscillator / Clock"),
    MONOSTABLE("Monostable", "One-Shot Timer Pulse")
}

@Composable
fun Timer555StudioTool() {
    val haptic = LocalHapticFeedback.current
    var timerMode by remember { mutableStateOf(Timer555Mode.ASTABLE) }

    var r1Str by remember { mutableStateOf("10") }
    var r2Str by remember { mutableStateOf("10") }
    var cStr by remember { mutableStateOf("10") }

    val scrollState = rememberScrollState()

    fun triggerHaptic() {
        if (UserSettings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val r1 = (r1Str.toDoubleOrNull() ?: 10.0) * 1000.0
    val r2 = (r2Str.toDoubleOrNull() ?: 10.0) * 1000.0
    val c = (cStr.toDoubleOrNull() ?: 10.0) * 1e-6

    // Calculations based on mode
    val freq = if (timerMode == Timer555Mode.ASTABLE) {
        if ((r1 + 2 * r2) * c > 0) 1.44 / ((r1 + 2 * r2) * c) else 0.0
    } else {
        0.0
    }

    val tHigh = if (timerMode == Timer555Mode.ASTABLE) {
        0.693 * (r1 + r2) * c
    } else {
        1.1 * r1 * c // Monostable pulse duration
    }

    val tLow = if (timerMode == Timer555Mode.ASTABLE) {
        0.693 * r2 * c
    } else {
        0.0
    }

    val tTotal = if (timerMode == Timer555Mode.ASTABLE) tHigh + tLow else tHigh
    val dutyCycle = if (timerMode == Timer555Mode.ASTABLE && tTotal > 0) (tHigh / tTotal) * 100.0 else 100.0

    // Virtual Oscilloscope Infinite Scrolling Animation (like a real oscilloscope sweep)
    val infiniteTransition = rememberInfiniteTransition(label = "oscilloscope_sweep")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_offset"
    )

    // Dynamic glowing beam pulse based on phase
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NE555 TIMER STUDIO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (timerMode == Timer555Mode.ASTABLE) "Continuous square wave astable multivibrator." else "Single-pulse triggered monostable circuit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Astable vs Monostable Mode Selector Pills
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ThemeSurface,
            border = BorderStroke(1.dp, ThemeOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Timer555Mode.entries.forEach { mode ->
                    val isSelected = timerMode == mode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ThemePrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                triggerHaptic()
                                timerMode = mode
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mode.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = mode.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (isSelected) Color(0xFF0F172A).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inputs Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = r1Str,
                onValueChange = { r1Str = it },
                label = { Text("R1 (kΩ)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (timerMode == Timer555Mode.ASTABLE) {
                OutlinedTextField(
                    value = r2Str,
                    onValueChange = { r2Str = it },
                    label = { Text("R2 (kΩ)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            OutlinedTextField(
                value = cStr,
                onValueChange = { cStr = it },
                label = { Text("C1 (µF)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LIVE WAVEFORM OSCILLOSCOPE CANVAS WITH CONTINUOUS SCROLLING SWEEP
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
            border = BorderStroke(1.dp, ThemeOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Glowing sweep indicator dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ThemeAccentGreenGlow.copy(alpha = glowAlpha))
                                .border(1.dp, ThemeAccentGreen, CircleShape)
                        )
                        Text(
                            text = "VIRTUAL OSCILLOSCOPE SWEEP",
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemePrimary,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ThemeAccentGreen.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, ThemeAccentGreen.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (timerMode == Timer555Mode.ASTABLE) "RUNNING (ASTABLE)" else "ONE-SHOT PULSE",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = ThemeAccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Waveform with scrolling phase offset and neon phosphor glow
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF070B12))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid lines (classic oscilloscope graticule)
                    for (x in 0..10) {
                        val gx = x * (w / 10f)
                        drawLine(Color(0xFF111D33), Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                    }
                    for (y in 0..4) {
                        val gy = y * (h / 4f)
                        drawLine(Color(0xFF111D33), Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                    }

                    val highY = h * 0.22f
                    val lowY = h * 0.78f

                    val path = Path()

                    if (timerMode == Timer555Mode.ASTABLE) {
                        val cycleWidth = (w / 3.2f).coerceAtLeast(30f)
                        val safeDuty = (dutyCycle.toFloat()).coerceIn(5f, 95f)
                        val highWidth = (cycleWidth * (safeDuty / 100f)).coerceAtLeast(5f)
                        val lowWidth = (cycleWidth - highWidth).coerceAtLeast(5f)

                        // Offset shifting continuously based on phaseOffset (creates continuous left-to-right sweep)
                        val shiftX = (phaseOffset * cycleWidth)
                        var curX = -cycleWidth + shiftX

                        path.moveTo(curX, lowY)

                        var safety = 0
                        while (curX < w + cycleWidth && safety < 30) {
                            path.lineTo(curX, highY)
                            curX += highWidth
                            path.lineTo(curX, highY)
                            path.lineTo(curX, lowY)
                            curX += lowWidth
                            path.lineTo(curX, lowY)
                            safety++
                        }
                    } else {
                        // Monostable: show steady LOW, falling trigger, pulse of length T, then returning to LOW
                        val triggerX = w * 0.18f
                        val pulseWidth = (w * 0.45f)
                        path.moveTo(0f, lowY)
                        path.lineTo(triggerX, lowY)
                        path.lineTo(triggerX, highY)
                        path.lineTo(triggerX + pulseWidth, highY)
                        path.lineTo(triggerX + pulseWidth, lowY)
                        path.lineTo(w, lowY)

                        // Draw moving scan dot along the pulse
                        val scanDotX = (phaseOffset * w)
                        drawCircle(
                            color = ThemeAccentGreenGlow,
                            radius = 4.dp.toPx(),
                            center = Offset(scanDotX, if (scanDotX >= triggerX && scanDotX <= triggerX + pulseWidth) highY else lowY)
                        )
                    }

                    // 1. Phosphor Outer Glow
                    drawPath(
                        path = path,
                        color = ThemeAccentGreenGlow.copy(alpha = 0.35f * glowAlpha),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // 2. Core Crisp Neon Beam
                    drawPath(
                        path = path,
                        color = ThemeAccentGreenGlow,
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Readouts
                if (timerMode == Timer555Mode.ASTABLE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FREQUENCY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (freq >= 1000) "${formatDouble(freq / 1000)} kHz" else "${formatDouble(freq)} Hz",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ThemePrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("DUTY CYCLE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${formatDouble(dutyCycle, 1)} %",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ThemeAccentGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = ThemeOutline)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("High (t1): ${formatDouble(tHigh * 1000)} ms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Low (t2): ${formatDouble(tLow * 1000)} ms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Period: ${formatDouble(tTotal * 1000)} ms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PULSE DURATION (T)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (tHigh >= 1.0) "${formatDouble(tHigh)} sec" else "${formatDouble(tHigh * 1000)} ms",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ThemePrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ThemePrimaryContainer,
                            border = BorderStroke(1.dp, ThemePrimary)
                        ) {
                            Text(
                                text = "T = 1.1 × R1 × C",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ThemePrimary
                            )
                        }
                    }
                }
            }
        }

        if (UserSettings.showFormulasAlways) {
            Spacer(modifier = Modifier.height(10.dp))
            CalculatorFormulaCard(
                title = if (timerMode == Timer555Mode.ASTABLE) "555 Astable Oscillator Theory" else "555 Monostable One-Shot Theory",
                formula = if (timerMode == Timer555Mode.ASTABLE) "f = 1.44 / ((R1 + 2×R2) × C) | Duty = ((R1 + R2) / (R1 + 2×R2)) × 100%" else "T_pulse = 1.1 × R1 × C (Time Output Pin 3 stays HIGH)",
                note = if (timerMode == Timer555Mode.ASTABLE) "Astable creates continuous square waves without external triggers." else "Pin 2 (Trigger) pulse goes LOW (< 1/3 Vcc) to initiate single timed output pulse."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =================================================================
// 5. LED RESISTOR & COMMERCIAL E12 DESIGNER
// =================================================================

@Composable
fun LedResistorDesignerTool() {
    val haptic = LocalHapticFeedback.current
    var vSupplyStr by remember { mutableStateOf("9.0") }
    var vForwardStr by remember { mutableStateOf("2.0") }
    var currentMaStr by remember { mutableStateOf("20") }

    val scrollState = rememberScrollState()

    val ledPresets = listOf(
        Triple("Red", 2.0, Color(0xFFEF4444)),
        Triple("Green", 2.2, Color(0xFF22C55E)),
        Triple("Blue", 3.2, Color(0xFF3B82F6)),
        Triple("White", 3.3, Color(0xFFF8FAFC)),
        Triple("Amber", 2.1, Color(0xFFF59E0B)),
        Triple("UV", 3.5, Color(0xFFA855F7))
    )

    val vSupply = vSupplyStr.toDoubleOrNull() ?: 9.0
    val vF = vForwardStr.toDoubleOrNull() ?: 2.0
    val iMa = currentMaStr.toDoubleOrNull() ?: 20.0
    val iA = iMa / 1000.0

    val rCalculated = if (vSupply > vF && iA > 0) (vSupply - vF) / iA else 0.0
    val powerWatts = if (vSupply > vF) (vSupply - vF) * iA else 0.0

    // Nearest standard commercial E12 resistor
    val e12Series = listOf(10, 12, 15, 18, 22, 27, 33, 39, 47, 56, 68, 82)
    val standardResistor = remember(rCalculated) {
        if (rCalculated <= 0) 0
        else {
            var mult = 1
            while (mult * 100 <= rCalculated) mult *= 10
            e12Series.map { it * mult }.firstOrNull { it >= rCalculated } ?: (e12Series.last() * mult)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "LED SERIES RESISTOR DESIGNER",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Calculates exact resistance, power dissipation, and recommends nearest standard E12 commercial value.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // LED Quick Color Pickers
        Text("SELECT LED FORWARD VOLTAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ledPresets) { (name, vf, col) ->
                val isSelected = vForwardStr == vf.toString()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) ThemePrimary.copy(alpha = 0.2f) else ThemeSurface,
                    border = BorderStroke(1.dp, if (isSelected) ThemePrimary else ThemeOutline),
                    modifier = Modifier.clickable {
                        if (UserSettings.hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        vForwardStr = vf.toString()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(col)
                        )
                        Text(
                            text = "$name (${vf}V)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = vSupplyStr,
                onValueChange = { vSupplyStr = it },
                label = { Text("Supply (V)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = vForwardStr,
                onValueChange = { vForwardStr = it },
                label = { Text("V_fwd (V)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = currentMaStr,
                onValueChange = { currentMaStr = it },
                label = { Text("Current (mA)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
            border = BorderStroke(1.dp, ThemeOutline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("RECOMMENDED COMMERCIAL RESISTOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (standardResistor > 0) "$standardResistor Ω" else "N/A",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = ThemePrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ThemeAccentGreen.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ThemeAccentGreen)
                    ) {
                        Text(
                            text = "E12 STANDARD",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemeAccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = ThemeOutline)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Calculated Exact", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${formatDouble(rCalculated)} Ω", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Power Dissipated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${formatDouble(powerWatts, 3)} W (1/4W OK)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ThemeAccentGreen)
                    }
                }
            }
        }

        if (UserSettings.showFormulasAlways) {
            Spacer(modifier = Modifier.height(10.dp))
            CalculatorFormulaCard(
                title = "LED Resistance & Power Formula",
                formula = "R = (V_supply - V_forward) / I_LED | P_resistor = (V_supply - V_forward) × I_LED",
                note = "Always round UP to the nearest standard commercial E12 value to prevent LED burnout."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =================================================================
// 6. OHM'S LAW 4-WAY SOLVER
// =================================================================

@Composable
fun OhmsLawStudioTool() {
    var vStr by remember { mutableStateOf("12") }
    var iStr by remember { mutableStateOf("0.5") }
    var rStr by remember { mutableStateOf("") }
    var pStr by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val v = vStr.toDoubleOrNull()
    val i = iStr.toDoubleOrNull()
    val r = rStr.toDoubleOrNull()
    val p = pStr.toDoubleOrNull()

    // Solve for missing values
    val calcV = when {
        v != null -> v
        i != null && r != null -> i * r
        p != null && i != null && i != 0.0 -> p / i
        p != null && r != null -> sqrt(p * r)
        else -> null
    }

    val calcI = when {
        i != null -> i
        v != null && r != null && r != 0.0 -> v / r
        p != null && v != null && v != 0.0 -> p / v
        p != null && r != null && r != 0.0 -> sqrt(p / r)
        else -> null
    }

    val calcR = when {
        r != null -> r
        v != null && i != null && i != 0.0 -> v / i
        v != null && p != null && p != 0.0 -> (v * v) / p
        p != null && i != null && i != 0.0 -> p / (i * i)
        else -> null
    }

    val calcP = when {
        p != null -> p
        v != null && i != null -> v * i
        v != null && r != null && r != 0.0 -> (v * v) / r
        i != null && r != null -> (i * i) * r
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "OHM'S LAW & POWER ANALYZER",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Enter any 2 known values to instantly compute the remaining electrical properties.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = vStr,
                onValueChange = { vStr = it },
                label = { Text("Voltage (V)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = iStr,
                onValueChange = { iStr = it },
                label = { Text("Current (A)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = rStr,
                onValueChange = { rStr = it },
                label = { Text("Resistance (Ω)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = pStr,
                onValueChange = { pStr = it },
                label = { Text("Power (W)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4-Card Electrical Readout Grid formatted according to UserSettings.decimalPrecision
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OhmResultBox("VOLTAGE (V)", if (calcV != null) "${formatDouble(calcV)} V" else "—", ThemePrimary, Modifier.weight(1f))
                OhmResultBox("CURRENT (I)", if (calcI != null) "${formatDouble(calcI, (UserSettings.decimalPrecision + 1).coerceAtMost(6))} A" else "—", ThemeSecondary, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OhmResultBox("RESISTANCE (R)", if (calcR != null) "${formatDouble(calcR)} Ω" else "—", ThemeAccentAmber, Modifier.weight(1f))
                OhmResultBox("POWER (P)", if (calcP != null) "${formatDouble(calcP)} W" else "—", ThemeAccentGreen, Modifier.weight(1f))
            }
        }

        if (UserSettings.showFormulasAlways) {
            Spacer(modifier = Modifier.height(10.dp))
            CalculatorFormulaCard(
                title = "Ohm's & Joule's Laws",
                formula = "V = I × R | I = V / R | R = V / I | P = V × I = I² × R = V² / R",
                note = "Linear relationship between electrical potential, current flow, resistance, and dissipated power."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OhmResultBox(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ThemeSurfaceCard,
        border = BorderStroke(1.dp, ThemeOutline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }
    }
}
