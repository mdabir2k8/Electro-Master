package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ComponentEntity
import com.example.ui.theme.*
import java.util.concurrent.ConcurrentHashMap

// Caches and static shapes to guarantee fast O(1) rendering during scroll
private val packageTypeCache = ConcurrentHashMap<String, String>()
private val tagsCache = ConcurrentHashMap<String, List<String>>()
private val CompCardShape = RoundedCornerShape(14.dp)
private val CompPackageShape = RoundedCornerShape(6.dp)
private val CompCatShape = RoundedCornerShape(10.dp)
private val CompSpecsShape = RoundedCornerShape(8.dp)
private val CompTagShape = RoundedCornerShape(5.dp)

private val componentCategories = listOf(
    "All" to Icons.Outlined.Widgets,
    "IC" to Icons.Outlined.Memory,
    "Sensor" to Icons.Outlined.Visibility,
    "Transistor" to Icons.Outlined.ElectricalServices,
    "Logic IC" to Icons.Outlined.AccountTree,
    "Voltage Regulator" to Icons.Outlined.Power,
    "MCU & Board" to Icons.Outlined.DeveloperBoard,
    "MOSFET" to Icons.Outlined.Bolt,
    "Diode" to Icons.Outlined.Sensors,
    "Opto & Relay" to Icons.Outlined.ToggleOn,
    "Display & Module" to Icons.Outlined.SmartDisplay,
    "Motor Driver" to Icons.Outlined.PrecisionManufacturing
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ComponentViewModel = viewModel(),
    onNavigateToCalculator: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val components by viewModel.components.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    var displayLimit by remember { mutableIntStateOf(35) }

    LaunchedEffect(searchQuery, selectedCategory) {
        displayLimit = 35
    }

    val categories = componentCategories

    val filteredComponents = remember(components, searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty() && selectedCategory == "All") {
            components
        } else {
            components.filter { item ->
                val matchesCategory = selectedCategory == "All" || item.category.contains(selectedCategory, ignoreCase = true)
                val matchesQuery = query.isEmpty() ||
                        item.name.lowercase().contains(query) ||
                        item.category.lowercase().contains(query) ||
                        item.pinout.lowercase().contains(query) ||
                        item.primaryFunction.lowercase().contains(query) ||
                        item.keySpecs.lowercase().contains(query) ||
                        item.useCase.lowercase().contains(query)
                matchesCategory && matchesQuery
            }
        }
    }

    val visibleComponents = remember(filteredComponents, displayLimit) {
        filteredComponents.take(displayLimit)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        // Search & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Lab Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = {
                    Text(
                        "Search 300+ ICs, Sensors, Transistors, Pins, Specs...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ThemePrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("component_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ThemeSurface,
                    unfocusedContainerColor = ThemeSurface,
                    focusedBorderColor = ThemePrimary,
                    unfocusedBorderColor = ThemeOutline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills (Smooth horizontal scroll without lazy overhead)
            val catScrollState = rememberScrollState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(catScrollState)
            ) {
                categories.forEach { (category, icon) ->
                    val isSelected = selectedCategory == category
                    val activeBg = if (isSelected) ThemePrimary.copy(alpha = 0.2f) else ThemeSurface
                    val activeBorder = if (isSelected) ThemePrimary else ThemeOutline
                    val activeContentColor = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = activeBg,
                        border = BorderStroke(1.dp, activeBorder),
                        modifier = Modifier
                            .testTag("filter_chip_$category")
                            .clickable { selectedCategory = category }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = activeContentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = activeContentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-status count line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredComponents.size} COMPONENTS FOUND (SHOWING ${visibleComponents.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "SPEC LEVEL: LAB GRADE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemeAccentGreen,
                    letterSpacing = 1.sp
                )
            }
        }

        if (filteredComponents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No components match \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try searching by part number (e.g., ESP32, 555, STM32, 7805) or category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = visibleComponents,
                    key = { it.id },
                    contentType = { "component_card" }
                ) { component ->
                    PremiumComponentCard(
                        component = component,
                        onOpenCalculator = {
                            when {
                                component.name.contains("555", ignoreCase = true) -> onNavigateToCalculator(3)
                                component.category.contains("Passive", ignoreCase = true) -> onNavigateToCalculator(2)
                                else -> onNavigateToCalculator(5)
                            }
                        }
                    )
                }

                if (filteredComponents.size > visibleComponents.size) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = { displayLimit += 35 },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ThemePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemePrimary)
                            ) {
                                Text(
                                    text = "Load More (${filteredComponents.size - visibleComponents.size} remaining)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

private fun getPackageType(name: String, pinout: String, category: String): String {
    return when {
        pinout.contains("8-DIP", true) || pinout.contains("DIP-8", true) || name.contains("555") || name.contains("358") || name.contains("386") || name.contains("072") || name.contains("082") || name.contains("393") || name.contains("5532") || name.contains("OP07") || name.contains("2822") || name.contains("620") || name.contains("485") || name.contains("6002") || name.contains("7660") -> "DIP-8"
        pinout.contains("14-DIP", true) || pinout.contains("DIP-14", true) || name.contains("324") || name.contains("339") || name.startsWith("74HC0") || name.startsWith("74HC14") || name.startsWith("74HC32") || name.startsWith("74HC86") || name.startsWith("74HC74") || name.contains("4066") || name.contains("4011") -> "DIP-14"
        pinout.contains("16-DIP", true) || name.contains("595") || name.contains("165") || name.contains("138") || name.contains("4051") || name.contains("4017") || name.contains("2003") || name.contains("L293") || name.contains("8574") || name.contains("232") || name.contains("2206") -> "DIP-16"
        pinout.contains("20-DIP", true) || name.contains("245") -> "DIP-20"
        pinout.contains("DIP-4", true) || name.contains("PC817") || name.contains("DB107") -> "DIP-4"
        pinout.contains("DIP-6", true) || name.contains("4N35") || name.contains("MOC") -> "DIP-6"
        pinout.contains("TO-92", true) || name.startsWith("BC") || name.startsWith("2N") || name.startsWith("SS8") || name.contains("431") || name.contains("78L05") || name.contains("18B20") || name.contains("A3144") || name.contains("BS170") || name.contains("C945") || name.contains("A733") || name.contains("S8050") || name.contains("S8550") -> "TO-92"
        pinout.contains("TO-220", true) || name.startsWith("IRF") || name.startsWith("IRL") || name.startsWith("TIP") || name.startsWith("78") || name.startsWith("79") || name.startsWith("LM317") || name.startsWith("LM337") || name.contains("MBR") || name.contains("FQP") || name.contains("STP") || name.contains("2030") || name.contains("LM78") || name.contains("LM79") || name.contains("BT1") || name.contains("BTA") -> "TO-220"
        pinout.contains("TO-126", true) || name.startsWith("BD1") -> "TO-126"
        pinout.contains("TO-247", true) || name.contains("250N") || name.contains("460") || name.contains("142") || name.contains("147") -> "TO-247"
        pinout.contains("SOT-23", true) || name.startsWith("AO3") || name.contains("2302") || name.contains("138") || name.contains("219") || name.contains("DW01") -> "SOT-23"
        pinout.contains("SOT-223", true) || name.startsWith("AMS1117") -> "SOT-223"
        pinout.contains("TO-3", true) || name == "2N3055" || name == "MJ2955" -> "TO-3"
        pinout.contains("DO-41", true) || name.startsWith("1N4") || name.startsWith("1N5819") || name.startsWith("UF") || name.startsWith("FR") || name.contains("5408") -> "DO-41"
        pinout.contains("DO-35", true) || name.contains("4148") || name.startsWith("BZX") || name.contains("DB3") -> "DO-35"
        category == "Sensor" || category.contains("Module") || category.contains("Board") || pinout.contains("Module", true) || name.startsWith("HC-") || name.startsWith("MQ-") || name.startsWith("ESP") || name.startsWith("STM") || name.startsWith("Arduino") || name.contains("Pico") -> "MODULE"
        else -> "DISCRETE"
    }
}

private fun getCategoryColor(category: String): Color {
    return when {
        category.contains("MOSFET", true) -> ThemeAccentCyan
        category.contains("Voltage Regulator", true) -> Color(0xFFF59E0B)
        category.contains("Logic IC", true) -> Color(0xFF818CF8)
        category.contains("Opto", true) -> Color(0xFFE879F9)
        category.contains("MCU", true) -> Color(0xFF38BDF8)
        category.contains("Motor", true) -> Color(0xFFF43F5E)
        category.contains("Display", true) -> Color(0xFF34D399)
        category.contains("IC", true) -> ThemePrimary
        category.contains("Transistor", true) -> ThemeSecondary
        category.contains("Diode", true) -> ThemeAccentGold
        category.contains("Sensor", true) -> ThemeAccentGreen
        else -> ThemePrimaryGlow
    }
}

@Composable
fun PremiumComponentCard(
    component: ComponentEntity,
    onOpenCalculator: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    val packageType = remember(component.id) {
        packageTypeCache.getOrPut(component.id) {
            getPackageType(component.name, component.pinout, component.category)
        }
    }

    val categoryColor = remember(component.category) {
        getCategoryColor(component.category)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("component_card_${component.id}"),
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
        shape = CompCardShape,
        border = BorderStroke(1.dp, ThemeOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Package badge, Title, and Category Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Package Pill
                    Box(
                        modifier = Modifier
                            .background(ThemeSurfaceElevated, CompPackageShape)
                            .border(1.dp, ThemeOutlineBright, CompPackageShape)
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = packageType,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ThemePrimary,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                // Category pill
                Box(
                    modifier = Modifier
                        .background(categoryColor.copy(alpha = 0.15f), CompCatShape)
                        .border(1.dp, categoryColor.copy(alpha = 0.4f), CompCatShape)
                        .padding(horizontal = 9.dp, vertical = 3.5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                        )
                        Text(
                            text = component.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Function Description
            Text(
                text = component.primaryFunction,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Key Specs Box - High performance clean formatted text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeSurfaceElevated, CompSpecsShape)
                    .border(1.dp, ThemeOutline, CompSpecsShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "SPECIFICATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemePrimary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = component.keySpecs,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pinout Preview / Interactive Pinout Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PINOUT ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemePrimary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Copy Pinout Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("${component.name} Pinout", component.pinout)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "${component.name} pinout copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Pinout",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Expand / Collapse visual diagram
                    Box(
                        modifier = Modifier
                            .background(
                                if (isExpanded) ThemePrimary.copy(alpha = 0.2f) else Color.Transparent,
                                CompPackageShape
                            )
                            .border(1.dp, if (isExpanded) ThemePrimary else ThemeOutline, CompPackageShape)
                            .clickable { isExpanded = !isExpanded }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "Hide Map" else "Visual Map",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isExpanded) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = if (isExpanded) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Standard pinout text
            Text(
                text = component.pinout,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            // Visual IC / Component Map rendered with smooth AnimatedVisibility
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(280)) + fadeIn(animationSpec = tween(280)),
                exit = shrinkVertically(animationSpec = tween(240)) + fadeOut(animationSpec = tween(240))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    VisualComponentPinoutMap(component = component, packageType = packageType)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Application Tags & Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Application tags with cached lookup
                val tags = remember(component.id) {
                    tagsCache.getOrPut(component.id) {
                        component.useCase.split(",").take(2).map { it.trim() }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(ThemeSurfaceElevated, CompTagShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Relevant Calculator shortcut if applicable
                if (component.name.contains("555") || component.category.contains("Passive") || component.name.contains("7805")) {
                    Box(
                        modifier = Modifier
                            .background(ThemePrimaryContainer, RoundedCornerShape(6.dp))
                            .border(1.dp, ThemePrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onOpenCalculator() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = ThemePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Calc",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ThemePrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseDynamicPins(pinoutStr: String): Map<Int, String> {
    val map = mutableMapOf<Int, String>()
    val cleanStr = if (pinoutStr.contains(":")) pinoutStr.substringAfter(":") else pinoutStr
    val tokens = cleanStr.split(",")
    val regex = Regex("""(?:Pin\s*)?(\d+)\s*[-:]\s*(.+)""", RegexOption.IGNORE_CASE)

    for (token in tokens) {
        val trimmed = token.trim()
        val match = regex.find(trimmed)
        if (match != null) {
            val num = match.groupValues[1].toIntOrNull()
            val name = match.groupValues[2].trim()
            if (num != null && name.isNotBlank()) {
                map[num] = name
            }
        }
    }

    if (map.isEmpty() && tokens.isNotEmpty()) {
        tokens.forEachIndexed { index, tok ->
            val clean = tok.trim()
            if (clean.isNotBlank()) {
                map[index + 1] = clean
            }
        }
    }
    return map
}

@Composable
fun VisualComponentPinoutMap(component: ComponentEntity, packageType: String) {
    val pinMap = remember(component.id, component.pinout) {
        parseDynamicPins(component.pinout)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ThemeSurfaceElevated,
        border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PACKAGE: $packageType ARCHITECTURE",
                style = MaterialTheme.typography.labelSmall,
                color = ThemePrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            when {
                packageType.startsWith("DIP", ignoreCase = true) -> {
                    val expectedCount = when (packageType) {
                        "DIP-4" -> 4
                        "DIP-6" -> 6
                        "DIP-8" -> 8
                        "DIP-14" -> 14
                        "DIP-16" -> 16
                        "DIP-20" -> 20
                        else -> {
                            val maxKey = pinMap.keys.maxOrNull() ?: 8
                            if (maxKey % 2 != 0) maxKey + 1 else maxKey
                        }
                    }
                    val half = (expectedCount / 2).coerceAtLeast(1)
                    val leftPins = (1..half).map { i ->
                        val label = pinMap[i] ?: "Pin $i"
                        "$i: $label"
                    }
                    val rightPins = (expectedCount downTo (half + 1)).map { j ->
                        val label = pinMap[j] ?: "Pin $j"
                        "$j: $label"
                    }

                    DipChipVisualizer(
                        title = component.name,
                        leftPins = leftPins,
                        rightPins = rightPins
                    )
                }
                packageType == "TO-92" -> {
                    val p1 = pinMap[1] ?: "Emitter"
                    val p2 = pinMap[2] ?: "Base"
                    val p3 = pinMap[3] ?: "Collector"
                    To92Visualizer(component.name, listOf("1: $p1", "2: $p2", "3: $p3"))
                }
                packageType == "TO-220" || packageType == "TO-126" || packageType == "TO-247" -> {
                    val p1 = pinMap[1] ?: (if (component.category.contains("MOSFET")) "Gate" else "Input / Base")
                    val p2 = pinMap[2] ?: (if (component.category.contains("MOSFET")) "Drain (Tab)" else "Collector / Tab")
                    val p3 = pinMap[3] ?: (if (component.category.contains("MOSFET")) "Source" else "Emitter / Out")
                    To220Visualizer(title = component.name, pins = listOf("1: $p1", "2: $p2", "3: $p3"))
                }
                else -> {
                    if (pinMap.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PARSED HARDWARE PINOUT (${pinMap.size} PINS):",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val pinEntries = pinMap.entries.sortedBy { it.key }
                            pinEntries.chunked(2).forEach { rowPins ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPins.forEach { (pinNum, pinLabel) ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ThemeSurface,
                                            border = BorderStroke(1.dp, ThemeOutline),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = ThemePrimary.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "P$pinNum",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ThemePrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    text = pinLabel,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    if (rowPins.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = component.pinout,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DipChipVisualizer(
    title: String,
    leftPins: List<String>,
    rightPins: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Pins Column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            leftPins.forEach { pin ->
                val isGnd = pin.contains("GND", ignoreCase = true) || pin.contains("VSS", ignoreCase = true)
                val isPwr = pin.contains("VCC", ignoreCase = true) || pin.contains("VDD", ignoreCase = true) || pin.contains("VIN", ignoreCase = true) || pin.contains("VS", ignoreCase = true) || isGnd
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isGnd) Color(0xFF1E293B) else if (isPwr) Color(0xFF3B1E1E) else Color(0xFF0F2238),
                    border = BorderStroke(1.dp, if (isGnd) Color.Gray else if (isPwr) ThemeAccentRed else ThemePrimary)
                ) {
                    Text(
                        text = pin,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isGnd) Color.LightGray else if (isPwr) ThemeAccentRed else ThemePrimary
                    )
                }
            }
        }

        // Chip Body Graphic
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(58.dp)
                .height((leftPins.size * 28 + 20).dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF11141C))
                .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp)
            ) {
                // Top Notch
                Box(
                    modifier = Modifier
                        .size(14.dp, 6.dp)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(Color(0xFF334155))
                )
                Text(
                    text = title.take(6),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        // Right Pins Column
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            rightPins.forEach { pin ->
                val isGnd = pin.contains("GND", ignoreCase = true) || pin.contains("VSS", ignoreCase = true)
                val isPwr = pin.contains("VCC", ignoreCase = true) || pin.contains("VDD", ignoreCase = true) || pin.contains("VIN", ignoreCase = true) || pin.contains("VS", ignoreCase = true) || isGnd
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isGnd) Color(0xFF1E293B) else if (isPwr) Color(0xFF3B1E1E) else Color(0xFF0F2238),
                    border = BorderStroke(1.dp, if (isGnd) Color.Gray else if (isPwr) ThemeAccentRed else ThemePrimary)
                ) {
                    Text(
                        text = pin,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isGnd) Color.LightGray else if (isPwr) ThemeAccentRed else ThemePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun To92Visualizer(title: String, pins: List<String>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TO-92 Semi-circle Package Representation
        Box(
            modifier = Modifier
                .size(60.dp, 36.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF475569), RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 4.dp, bottomEnd = 4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.White)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pins.forEach { pin ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F1E36),
                    border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = pin,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = ThemePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun To220Visualizer(title: String, pins: List<String>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Metallic Heat Sink Tab
        Box(
            modifier = Modifier
                .size(54.dp, 16.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(Color(0xFF64748B))
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
            )
        }
        // Black Epoxy Body
        Box(
            modifier = Modifier
                .size(64.dp, 32.dp)
                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(Color(0xFF111827))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.White)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pins.forEach { pin ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F1E36),
                    border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = pin,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = ThemePrimary
                    )
                }
            }
        }
    }
}
