package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.UserSettings
import com.example.ui.theme.*

private val PinCardShape = RoundedCornerShape(10.dp)
private val BoardChipShape = RoundedCornerShape(10.dp)
private val BoardCardShape = RoundedCornerShape(14.dp)
private val PinFilterChipShape = RoundedCornerShape(6.dp)
private val GateChipShape = RoundedCornerShape(8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(modifier: Modifier = Modifier) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf(
        "Board Pinouts" to Icons.Outlined.DeveloperBoard,
        "Logic Simulator" to Icons.Outlined.AccountTree,
        "Formulas & Laws" to Icons.Outlined.AutoStories
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        // Section Selector Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = ThemeSurface,
            contentColor = ThemePrimary,
            divider = { HorizontalDivider(color = ThemeOutline) }
        ) {
            sections.forEachIndexed { index, (title, icon) ->
                val isSelected = selectedSection == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedSection = index },
                    modifier = Modifier.testTag("ref_tab_$index"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 10.dp)
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            when (selectedSection) {
                0 -> InteractiveBoardPinoutsSection()
                1 -> InteractiveLogicGateSimulatorSection()
                2 -> AdvancedFormulasSection()
            }
        }
    }
}

// =================================================================
// 1. INTERACTIVE BOARD PINOUT MAPS & FILTER
// =================================================================

@Immutable
data class BoardPin(val pinName: String, val function: String, val type: PinType)
enum class PinType { POWER, GROUND, DIGITAL, ANALOG, COMM }

@Immutable
data class DevelopmentBoard(
    val id: String,
    val name: String,
    val mcu: String,
    val voltage: String,
    val pinCount: String,
    val pins: List<BoardPin>
)

val developmentBoards = listOf(
    DevelopmentBoard(
        id = "uno",
        name = "Arduino Uno R3",
        mcu = "ATmega328P (16 MHz)",
        voltage = "5V Logic (7-12V Vin)",
        pinCount = "14 Digital / 6 Analog",
        pins = listOf(
            BoardPin("Pin 0 (RX)", "Hardware UART Receive", PinType.COMM),
            BoardPin("Pin 1 (TX)", "Hardware UART Transmit", PinType.COMM),
            BoardPin("Pin 2", "Digital I/O / External Interrupt 0", PinType.DIGITAL),
            BoardPin("Pin 3 (~)", "Digital I/O / 8-bit Timer2 PWM / Ext Interrupt 1", PinType.DIGITAL),
            BoardPin("Pin 4", "Digital I/O", PinType.DIGITAL),
            BoardPin("Pin 5 (~)", "Digital I/O / 8-bit Timer0 PWM", PinType.DIGITAL),
            BoardPin("Pin 6 (~)", "Digital I/O / 8-bit Timer0 PWM", PinType.DIGITAL),
            BoardPin("Pin 7", "Digital I/O", PinType.DIGITAL),
            BoardPin("Pin 8", "Digital I/O", PinType.DIGITAL),
            BoardPin("Pin 9 (~)", "Digital I/O / 16-bit Timer1 PWM", PinType.DIGITAL),
            BoardPin("Pin 10 (~)", "Digital I/O / Timer1 PWM / SPI SS", PinType.COMM),
            BoardPin("Pin 11 (~)", "Digital I/O / Timer2 PWM / SPI MOSI", PinType.COMM),
            BoardPin("Pin 12", "Digital I/O / SPI MISO", PinType.COMM),
            BoardPin("Pin 13", "Digital I/O / SPI SCK / Built-in LED", PinType.COMM),
            BoardPin("GND", "Power Ground Rail", PinType.GROUND),
            BoardPin("AREF", "Analog Reference Voltage", PinType.ANALOG),
            BoardPin("A0", "10-bit ADC Analog Input (0-5V)", PinType.ANALOG),
            BoardPin("A1", "10-bit ADC Analog Input", PinType.ANALOG),
            BoardPin("A2", "10-bit ADC Analog Input", PinType.ANALOG),
            BoardPin("A3", "10-bit ADC Analog Input", PinType.ANALOG),
            BoardPin("A4 (SDA)", "10-bit ADC / I2C Data", PinType.COMM),
            BoardPin("A5 (SCL)", "10-bit ADC / I2C Clock", PinType.COMM),
            BoardPin("VIN", "Raw Input Voltage (7-12V DC)", PinType.POWER),
            BoardPin("5V", "Regulated 5V Rail (Max 500mA)", PinType.POWER),
            BoardPin("3.3V", "Regulated 3.3V Rail (Max 150mA)", PinType.POWER),
            BoardPin("RESET", "Active Low Reset Input", PinType.DIGITAL)
        )
    ),
    DevelopmentBoard(
        id = "esp32",
        name = "ESP32 DevKit V1",
        mcu = "Tensilica Dual-Core LX6 (240 MHz)",
        voltage = "3.3V Logic (5V Vin)",
        pinCount = "30 Pins / Wi-Fi & BT",
        pins = listOf(
            BoardPin("GPIO 21 (SDA)", "Hardware I2C Data", PinType.COMM),
            BoardPin("GPIO 22 (SCL)", "Hardware I2C Clock", PinType.COMM),
            BoardPin("GPIO 18 (SCK)", "VSPI Bus Clock", PinType.COMM),
            BoardPin("GPIO 19 (MISO)", "VSPI Master In", PinType.COMM),
            BoardPin("GPIO 23 (MOSI)", "VSPI Master Out", PinType.COMM),
            BoardPin("GPIO 25 (DAC1)", "8-bit Digital to Analog Converter", PinType.ANALOG),
            BoardPin("GPIO 26 (DAC2)", "8-bit Digital to Analog Converter", PinType.ANALOG),
            BoardPin("GPIO 32 (ADC1_CH4)", "12-bit ADC (Safe with Wi-Fi)", PinType.ANALOG),
            BoardPin("GPIO 33 (ADC1_CH5)", "12-bit ADC (Safe with Wi-Fi)", PinType.ANALOG),
            BoardPin("GPIO 34 (ADC1_CH6)", "12-bit ADC (Input Only)", PinType.ANALOG),
            BoardPin("GPIO 35 (ADC1_CH7)", "12-bit ADC (Input Only)", PinType.ANALOG),
            BoardPin("GPIO 36 (VP)", "12-bit ADC / Sensor VP (Input Only)", PinType.ANALOG),
            BoardPin("GPIO 39 (VN)", "12-bit ADC / Sensor VN (Input Only)", PinType.ANALOG),
            BoardPin("GPIO 2", "Built-in Blue LED / Boot Strapping", PinType.DIGITAL),
            BoardPin("GPIO 4", "Touch Sensor 0 / Capacitive Pin", PinType.DIGITAL),
            BoardPin("GPIO 15", "Touch Sensor 3 / PWM", PinType.DIGITAL),
            BoardPin("3V3", "3.3V Regulated Output Rail", PinType.POWER),
            BoardPin("GND", "Power Ground Rail", PinType.GROUND),
            BoardPin("VIN / 5V", "5V USB / External Power Input", PinType.POWER)
        )
    ),
    DevelopmentBoard(
        id = "pico",
        name = "Raspberry Pi Pico",
        mcu = "RP2040 Dual ARM Cortex-M0+",
        voltage = "3.3V Logic",
        pinCount = "40 Pins / 26 GPIO",
        pins = listOf(
            BoardPin("GP0 / GP1", "UART0 TX / RX or I2C0 SDA / SCL", PinType.COMM),
            BoardPin("GP2 / GP3", "I2C1 SDA / SCL", PinType.COMM),
            BoardPin("GP4 / GP5", "UART1 TX / RX or I2C0 SDA / SCL", PinType.COMM),
            BoardPin("GP16 / GP17 / GP18 / GP19", "SPI0 RX / CSn / SCK / TX", PinType.COMM),
            BoardPin("GP26 (ADC0)", "12-bit ADC Channel 0", PinType.ANALOG),
            BoardPin("GP27 (ADC1)", "12-bit ADC Channel 1", PinType.ANALOG),
            BoardPin("GP28 (ADC2)", "12-bit ADC Channel 2", PinType.ANALOG),
            BoardPin("GP25", "On-board User LED", PinType.DIGITAL),
            BoardPin("3V3 (OUT)", "3.3V System Power Output", PinType.POWER),
            BoardPin("VBUS", "Micro-USB 5V Supply Input", PinType.POWER),
            BoardPin("VSYS", "System Main Input Voltage (1.8V - 5.5V)", PinType.POWER),
            BoardPin("GND", "System Ground Rail", PinType.GROUND)
        )
    ),
    DevelopmentBoard(
        id = "esp8266",
        name = "ESP8266 NodeMCU V3",
        mcu = "Tensilica L106 (80 MHz)",
        voltage = "3.3V Logic (5V Vin)",
        pinCount = "11 GPIOs / 1 ADC",
        pins = listOf(
            BoardPin("D1 (GPIO 5)", "I2C SCL Clock", PinType.COMM),
            BoardPin("D2 (GPIO 4)", "I2C SDA Data", PinType.COMM),
            BoardPin("D5 (GPIO 14)", "SPI SCK Bus Clock", PinType.COMM),
            BoardPin("D6 (GPIO 12)", "SPI MISO Bus Master In", PinType.COMM),
            BoardPin("D7 (GPIO 13)", "SPI MOSI Bus Master Out", PinType.COMM),
            BoardPin("D9 (RXD0)", "Hardware Serial UART RX", PinType.COMM),
            BoardPin("D10 (TXD0)", "Hardware Serial UART TX", PinType.COMM),
            BoardPin("A0 (ADC0)", "10-bit ADC Input (Max 3.3V)", PinType.ANALOG),
            BoardPin("D0 (GPIO 16)", "User LED / Deep Sleep Wake Pin", PinType.DIGITAL),
            BoardPin("3V3", "3.3V Output Rail", PinType.POWER),
            BoardPin("GND", "Power Ground Rail", PinType.GROUND),
            BoardPin("VIN", "5V Power Input", PinType.POWER)
        )
    ),
    DevelopmentBoard(
        id = "stm32",
        name = "STM32 (Blue Pill)",
        mcu = "ARM Cortex-M3 (72 MHz)",
        voltage = "3.3V Logic (5V Tolerant)",
        pinCount = "40 Pins / Dual 12-bit ADC",
        pins = listOf(
            BoardPin("PC13", "Built-in User LED (Active Low)", PinType.DIGITAL),
            BoardPin("PA0 (ADC0)", "12-bit ADC Channel 0 / WKUP", PinType.ANALOG),
            BoardPin("PA1 (ADC1)", "12-bit ADC Channel 1 / Timer2 PWM", PinType.ANALOG),
            BoardPin("PA2 (ADC2)", "12-bit ADC / USART2 TX", PinType.COMM),
            BoardPin("PA3 (ADC3)", "12-bit ADC / USART2 RX", PinType.COMM),
            BoardPin("PA4 (NSS1)", "SPI1 Slave Select / ADC4", PinType.COMM),
            BoardPin("PA5 (SCK1)", "SPI1 Clock (18 MHz max)", PinType.COMM),
            BoardPin("PA6 (MISO1)", "SPI1 Master In / Timer3 PWM", PinType.COMM),
            BoardPin("PA7 (MOSI1)", "SPI1 Master Out / Timer3 PWM", PinType.COMM),
            BoardPin("PA9 (TX1)", "USART1 Transmit (5V Tolerant)", PinType.COMM),
            BoardPin("PA10 (RX1)", "USART1 Receive (5V Tolerant)", PinType.COMM),
            BoardPin("PA11 (USB D-)", "Hardware USB 2.0 Full Speed", PinType.COMM),
            BoardPin("PA12 (USB D+)", "Hardware USB 2.0 Full Speed", PinType.COMM),
            BoardPin("PB6 (SCL1)", "I2C1 Clock (Fast Mode 400kHz)", PinType.COMM),
            BoardPin("PB7 (SDA1)", "I2C1 Data (5V Tolerant)", PinType.COMM),
            BoardPin("PB8 (CAN_RX)", "Hardware CAN 2.0B Bus Receive", PinType.COMM),
            BoardPin("PB9 (CAN_TX)", "Hardware CAN 2.0B Bus Transmit", PinType.COMM),
            BoardPin("SWDIO / SWCLK", "Serial Wire Debug SWD Header", PinType.DIGITAL),
            BoardPin("3V3", "3.3V System Output Rail", PinType.POWER),
            BoardPin("GND", "System Ground Rail", PinType.GROUND),
            BoardPin("5V", "USB / External 5V Power Input", PinType.POWER)
        )
    ),
    DevelopmentBoard(
        id = "mega",
        name = "Arduino Mega 2560",
        mcu = "ATmega2560 (16 MHz)",
        voltage = "5V Logic (7-12V Vin)",
        pinCount = "54 Digital / 16 Analog",
        pins = listOf(
            BoardPin("Pin 0-1 (UART0)", "Serial USB Programming TX/RX", PinType.COMM),
            BoardPin("Pin 14-15 (UART3)", "Serial3 Hardware UART", PinType.COMM),
            BoardPin("Pin 16-17 (UART2)", "Serial2 Hardware UART", PinType.COMM),
            BoardPin("Pin 18-19 (UART1)", "Serial1 Hardware UART", PinType.COMM),
            BoardPin("Pin 20 (SDA)", "Hardware I2C Data", PinType.COMM),
            BoardPin("Pin 21 (SCL)", "Hardware I2C Clock", PinType.COMM),
            BoardPin("Pin 50 (MISO)", "Hardware SPI Master In", PinType.COMM),
            BoardPin("Pin 51 (MOSI)", "Hardware SPI Master Out", PinType.COMM),
            BoardPin("Pin 52 (SCK)", "Hardware SPI Bus Clock", PinType.COMM),
            BoardPin("Pin 53 (SS)", "Hardware SPI Slave Select", PinType.COMM),
            BoardPin("Pin 2-13 (PWM)", "8-bit Timer PWM Outputs", PinType.DIGITAL),
            BoardPin("A0-A7", "10-bit ADC Analog Inputs 0 to 7", PinType.ANALOG),
            BoardPin("A8-A15", "10-bit ADC Analog Inputs 8 to 15", PinType.ANALOG),
            BoardPin("5V", "Regulated 5V Rail (Max 800mA)", PinType.POWER),
            BoardPin("3.3V", "Regulated 3.3V Rail (Max 150mA)", PinType.POWER),
            BoardPin("GND", "Power Ground Rail", PinType.GROUND),
            BoardPin("VIN", "Raw DC Power Input (7-12V)", PinType.POWER)
        )
    )
)

@Composable
fun InteractiveBoardPinoutsSection() {
    var selectedBoardId by remember { mutableStateOf(UserSettings.defaultBoard.ifBlank { "uno" }) }
    var pinFilter by remember { mutableStateOf("ALL") }
    var searchKeyword by remember { mutableStateOf("") }

    val activeBoard = remember(selectedBoardId) {
        developmentBoards.firstOrNull { it.id == selectedBoardId } ?: developmentBoards.first()
    }

    val filteredPins = remember(activeBoard, pinFilter, searchKeyword) {
        activeBoard.pins.filter { pin ->
            val matchesFilter = when (pinFilter) {
                "POWER" -> pin.type == PinType.POWER || pin.type == PinType.GROUND
                "DIGITAL" -> pin.type == PinType.DIGITAL
                "ANALOG" -> pin.type == PinType.ANALOG
                "COMM" -> pin.type == PinType.COMM
                else -> true
            }
            val matchesSearch = searchKeyword.isBlank() ||
                    pin.pinName.contains(searchKeyword, ignoreCase = true) ||
                    pin.function.contains(searchKeyword, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Board Selector (Smooth horizontal scroll without lazy overhead)
        val boardScrollState = rememberScrollState()
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(boardScrollState)
        ) {
            developmentBoards.forEach { board ->
                val isSelected = board.id == selectedBoardId
                Surface(
                    shape = BoardChipShape,
                    color = if (isSelected) ThemePrimary.copy(alpha = 0.2f) else ThemeSurface,
                    border = BorderStroke(1.dp, if (isSelected) ThemePrimary else ThemeOutline),
                    modifier = Modifier.clickable { selectedBoardId = board.id }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.DeveloperBoard,
                            contentDescription = null,
                            tint = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = board.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Board Specs Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = BoardCardShape,
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
            border = BorderStroke(1.dp, ThemeOutline)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeBoard.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = ThemePrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = activeBoard.voltage,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemeAccentGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${activeBoard.mcu} • ${activeBoard.pinCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pin Search Bar
        OutlinedTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            placeholder = {
                Text(
                    "Search pin name or function (e.g., PWM, I2C, A0, SCK)...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search Pins",
                    tint = ThemePrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchKeyword.isNotEmpty()) {
                    IconButton(onClick = { searchKeyword = "" }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pinout_search_input"),
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

        // Quick Pin Filters & Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "POWER", "DIGITAL", "ANALOG", "COMM").forEach { filter ->
                val isSelected = pinFilter == filter
                Surface(
                    shape = PinFilterChipShape,
                    color = if (isSelected) ThemePrimary else ThemeSurface,
                    modifier = Modifier.clickable { pinFilter = filter }
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pin List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredPins,
                key = { it.pinName },
                contentType = { "pin_card" }
            ) { pin ->
                val (badgeColor, typeLabel) = when (pin.type) {
                    PinType.POWER -> Pair(ThemeAccentRed, "POWER")
                    PinType.GROUND -> Pair(Color(0xFF64748B), "GND")
                    PinType.DIGITAL -> Pair(ThemePrimary, "GPIO")
                    PinType.ANALOG -> Pair(ThemeAccentAmber, "ADC")
                    PinType.COMM -> Pair(ThemeAccentPurple, "BUS")
                }

                Surface(
                    shape = PinCardShape,
                    color = ThemeSurface,
                    border = BorderStroke(1.dp, ThemeOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = badgeColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = typeLabel,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = pin.pinName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = pin.function,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
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

// =================================================================
// 2. INTERACTIVE LOGIC GATE SIMULATOR
// =================================================================

data class GateDefinition(
    val name: String,
    val expression: String,
    val icNumber: String,
    val calculate: (Boolean, Boolean) -> Boolean,
    val truthTable: List<Triple<Int, Int, Int>>
)

val logicGates = listOf(
    GateDefinition(
        name = "AND Gate",
        expression = "Y = A • B",
        icNumber = "74HC08",
        calculate = { a, b -> a && b },
        truthTable = listOf(Triple(0, 0, 0), Triple(0, 1, 0), Triple(1, 0, 0), Triple(1, 1, 1))
    ),
    GateDefinition(
        name = "OR Gate",
        expression = "Y = A + B",
        icNumber = "74HC32",
        calculate = { a, b -> a || b },
        truthTable = listOf(Triple(0, 0, 0), Triple(0, 1, 1), Triple(1, 0, 1), Triple(1, 1, 1))
    ),
    GateDefinition(
        name = "NOT Gate",
        expression = "Y = A'",
        icNumber = "74HC04",
        calculate = { a, _ -> !a },
        truthTable = listOf(Triple(0, 0, 1), Triple(1, 0, 0))
    ),
    GateDefinition(
        name = "NAND Gate",
        expression = "Y = (A • B)'",
        icNumber = "74HC00",
        calculate = { a, b -> !(a && b) },
        truthTable = listOf(Triple(0, 0, 1), Triple(0, 1, 1), Triple(1, 0, 1), Triple(1, 1, 0))
    ),
    GateDefinition(
        name = "NOR Gate",
        expression = "Y = (A + B)'",
        icNumber = "74HC02",
        calculate = { a, b -> !(a || b) },
        truthTable = listOf(Triple(0, 0, 1), Triple(0, 1, 0), Triple(1, 0, 0), Triple(1, 1, 0))
    ),
    GateDefinition(
        name = "XOR Gate",
        expression = "Y = A ⊕ B",
        icNumber = "74HC86",
        calculate = { a, b -> a xor b },
        truthTable = listOf(Triple(0, 0, 0), Triple(0, 1, 1), Triple(1, 0, 1), Triple(1, 1, 0))
    ),
    GateDefinition(
        name = "XNOR Gate",
        expression = "Y = (A ⊕ B)'",
        icNumber = "74HC266",
        calculate = { a, b -> !(a xor b) },
        truthTable = listOf(Triple(0, 0, 1), Triple(0, 1, 0), Triple(1, 0, 0), Triple(1, 1, 1))
    )
)

@Composable
fun InteractiveLogicGateSimulatorSection() {
    var selectedGateIdx by remember { mutableIntStateOf(0) }
    var switchA by remember { mutableStateOf(false) }
    var switchB by remember { mutableStateOf(false) }

    val activeGate = logicGates[selectedGateIdx]
    val outputState = activeGate.calculate(switchA, switchB)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "DIGITAL LOGIC GATE SIMULATOR",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Select logic gate, toggle physical inputs A & B, and inspect real-time outputs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Gate Selector Chips (Smooth horizontal scroll)
        val gateScrollState = rememberScrollState()
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(gateScrollState)
        ) {
            logicGates.forEachIndexed { idx, gate ->
                val isSelected = selectedGateIdx == idx
                Surface(
                    shape = GateChipShape,
                    color = if (isSelected) ThemePrimary.copy(alpha = 0.2f) else ThemeSurface,
                    border = BorderStroke(1.dp, if (isSelected) ThemePrimary else ThemeOutline),
                    modifier = Modifier.clickable { selectedGateIdx = idx }
                ) {
                    Text(
                        text = gate.name.replace(" Gate", ""),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) ThemePrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INTERACTIVE GATE SIMULATOR CARD
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
                    Column {
                        Text(
                            text = activeGate.name.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = ThemePrimary
                        )
                        Text(
                            text = "Standard IC: ${activeGate.icNumber} • ${activeGate.expression}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Virtual Output LED Indicator with Radial Glow
                    val ledColor by animateColorAsState(
                        targetValue = if (outputState) ThemeAccentGreenGlow else Color(0xFF1E293B),
                        label = "ledColor"
                    )
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (outputState) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                ThemeAccentGreenGlow.copy(alpha = 0.55f),
                                                ThemeAccentGreen.copy(alpha = 0.18f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ledColor)
                                    .border(
                                        2.dp,
                                        if (outputState) ThemeAccentGreen else Color(0xFF475569),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (outputState) "1" else "0",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (outputState) Color(0xFF0F172A) else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (outputState) "HIGH (1)" else "LOW (0)",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                color = if (outputState) ThemeAccentGreen else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive ANSI/IEEE Logic Gate Circuit Diagram with Animated Flow
                LogicGateCircuitCanvas(
                    gateName = activeGate.name,
                    inputA = switchA,
                    inputB = switchB,
                    outputY = outputState
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Inputs: Switch A & Switch B
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch A
                    InteractiveSwitch("Input A", switchA) { switchA = !switchA }

                    if (activeGate.name != "NOT Gate") {
                        // Switch B
                        InteractiveSwitch("Input B", switchB) { switchB = !switchB }
                    }

                    // Logic Output Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ThemeSurfaceElevated,
                        border = BorderStroke(1.dp, if (outputState) ThemeAccentGreen else ThemeOutline)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("OUTPUT Y", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (outputState) "1" else "0",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
                                fontWeight = FontWeight.Black,
                                color = if (outputState) ThemeAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REAL-TIME HIGHLIGHTED TRUTH TABLE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
            border = BorderStroke(1.dp, ThemeOutline)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "TRUTH TABLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemePrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("INPUT A", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (activeGate.name != "NOT Gate") {
                        Text("INPUT B", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("OUTPUT Y", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Table Rows
                activeGate.truthTable.forEach { (a, b, y) ->
                    val isActiveRow = if (activeGate.name == "NOT Gate") {
                        (if (switchA) 1 else 0) == a
                    } else {
                        (if (switchA) 1 else 0) == a && (if (switchB) 1 else 0) == b
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActiveRow) ThemePrimary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                width = if (isActiveRow) 1.dp else 0.dp,
                                color = if (isActiveRow) ThemePrimary else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$a",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isActiveRow) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (isActiveRow) ThemePrimary else MaterialTheme.colorScheme.onSurface
                        )
                        if (activeGate.name != "NOT Gate") {
                            Text(
                                text = "$b",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isActiveRow) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                color = if (isActiveRow) ThemePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "$y",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (y == 1) ThemeAccentGreen else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveSwitch(label: String, state: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (state) ThemePrimary.copy(alpha = 0.25f) else ThemeSurfaceElevated,
        border = BorderStroke(1.5.dp, if (state) ThemePrimary else ThemeOutline),
        modifier = Modifier.clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (state) ThemePrimary else ThemeSurface
            ) {
                Text(
                    text = if (state) "1 (HIGH)" else "0 (LOW)",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (state) {
                        if (ThemeManager.currentTheme == AppTheme.WHITE) Color.White else ThemeBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun LogicGateCircuitCanvas(
    gateName: String,
    inputA: Boolean,
    inputB: Boolean,
    outputY: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wire_flow_anim")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_phase"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070B14))
            .border(1.dp, ThemeOutline, RoundedCornerShape(12.dp))
    ) {
        val w = size.width
        val h = size.height
        val isNot = gateName.startsWith("NOT", ignoreCase = true)
        val isNand = gateName.startsWith("NAND", ignoreCase = true)
        val isNor = gateName.startsWith("NOR", ignoreCase = true)
        val isXor = gateName.startsWith("XOR", ignoreCase = true)
        val isXnor = gateName.startsWith("XNOR", ignoreCase = true)
        val isOrFamily = isNor || isXor || isXnor || gateName.startsWith("OR", ignoreCase = true)

        val gateCenterX = w * 0.48f
        val gateCenterY = h / 2f
        val gateW = 64.dp.toPx()
        val gateH = 52.dp.toPx()
        val gateLeft = gateCenterX - gateW / 2f
        val gateRight = gateCenterX + gateW / 2f
        val gateTop = gateCenterY - gateH / 2f
        val gateBottom = gateCenterY + gateH / 2f

        val inAY = if (isNot) gateCenterY else gateCenterY - 14.dp.toPx()
        val inBY = gateCenterY + 14.dp.toPx()
        val outY = gateCenterY

        val wireStartX = 24.dp.toPx()
        val outEndX = w - 46.dp.toPx()

        val activeDash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), -flowPhase)

        // Helper to draw wire with neon pulses when HIGH
        fun drawWire(startX: Float, startY: Float, endX: Float, endY: Float, isHigh: Boolean) {
            val wirePath = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            if (isHigh) {
                // Neon glow halo
                drawPath(
                    path = wirePath,
                    color = ThemeAccentGreenGlow.copy(alpha = 0.35f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                // Base glowing trace
                drawPath(
                    path = wirePath,
                    color = ThemeAccentGreen,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                // Animated flowing neon dashes
                drawPath(
                    path = wirePath,
                    color = Color.White,
                    style = Stroke(width = 2.5.dp.toPx(), pathEffect = activeDash, cap = StrokeCap.Round)
                )
            } else {
                drawPath(
                    path = wirePath,
                    color = Color(0xFF26334D),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Draw input wires
        drawWire(wireStartX, inAY, gateLeft, inAY, inputA)
        if (!isNot) {
            drawWire(wireStartX, inBY, gateLeft, inBY, inputB)
        }

        // Draw output wire from gate to LED terminal
        val gateActualRight = if (isNand || isNor || isXnor || isNot) gateRight + 8.dp.toPx() else gateRight
        drawWire(gateActualRight, outY, outEndX, outY, outputY)

        // Input Terminal Nodes
        drawCircle(
            color = if (inputA) ThemeAccentGreen else Color(0xFF64748B),
            radius = 4.dp.toPx(),
            center = Offset(wireStartX, inAY)
        )
        if (!isNot) {
            drawCircle(
                color = if (inputB) ThemeAccentGreen else Color(0xFF64748B),
                radius = 4.dp.toPx(),
                center = Offset(wireStartX, inBY)
            )
        }

        // Output LED at the end with Radial Glow Shadow when HIGH
        val ledCenterX = outEndX + 16.dp.toPx()
        val ledCenterY = outY

        if (outputY) {
            // Radial Glow Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ThemeAccentGreenGlow.copy(alpha = 0.65f * glowPulse),
                        ThemeAccentGreen.copy(alpha = 0.25f * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(ledCenterX, ledCenterY),
                    radius = 26.dp.toPx()
                ),
                radius = 26.dp.toPx(),
                center = Offset(ledCenterX, ledCenterY)
            )
        }

        // LED Diode body
        drawCircle(
            color = if (outputY) ThemeAccentGreenGlow else Color(0xFF1E293B),
            radius = 10.dp.toPx(),
            center = Offset(ledCenterX, ledCenterY)
        )
        drawCircle(
            color = if (outputY) ThemeAccentGreen else Color(0xFF475569),
            radius = 10.dp.toPx(),
            center = Offset(ledCenterX, ledCenterY),
            style = Stroke(width = 2.dp.toPx())
        )

        // Standard ANSI/IEEE Gate Symbol Drawing
        val gatePath = Path()
        val gateColor = ThemePrimary
        val gateFillColor = ThemePrimary.copy(alpha = 0.12f)

        if (isNot) {
            // Triangle NOT gate pointing right
            gatePath.moveTo(gateLeft, gateTop)
            gatePath.lineTo(gateRight, gateCenterY)
            gatePath.lineTo(gateLeft, gateBottom)
            gatePath.close()
        } else if (isOrFamily) {
            // OR / NOR / XOR / XNOR gate
            gatePath.moveTo(gateLeft, gateTop)
            gatePath.cubicTo(
                gateLeft + gateW * 0.45f, gateTop,
                gateRight - gateW * 0.1f, gateCenterY - 4.dp.toPx(),
                gateRight, gateCenterY
            )
            gatePath.cubicTo(
                gateRight - gateW * 0.1f, gateCenterY + 4.dp.toPx(),
                gateLeft + gateW * 0.45f, gateBottom,
                gateLeft, gateBottom
            )
            gatePath.cubicTo(
                gateLeft + gateW * 0.25f, gateCenterY + gateH * 0.3f,
                gateLeft + gateW * 0.25f, gateCenterY - gateH * 0.3f,
                gateLeft, gateTop
            )
            gatePath.close()

            // If XOR or XNOR: draw additional curved input line
            if (isXor || isXnor) {
                val xorBackPath = Path().apply {
                    val xorLeft = gateLeft - 7.dp.toPx()
                    moveTo(xorLeft, gateTop)
                    cubicTo(
                        xorLeft + gateW * 0.25f, gateCenterY - gateH * 0.3f,
                        xorLeft + gateW * 0.25f, gateCenterY + gateH * 0.3f,
                        xorLeft, gateBottom
                    )
                }
                drawPath(
                    path = xorBackPath,
                    color = gateColor,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        } else {
            // AND / NAND Gate: Flat back, straight horizontal sides, curved front
            gatePath.moveTo(gateLeft, gateTop)
            gatePath.lineTo(gateCenterX, gateTop)
            gatePath.cubicTo(
                gateRight, gateTop,
                gateRight, gateBottom,
                gateCenterX, gateBottom
            )
            gatePath.lineTo(gateLeft, gateBottom)
            gatePath.close()
        }

        // Fill and outline gate body
        drawPath(path = gatePath, color = gateFillColor)
        drawPath(
            path = gatePath,
            color = gateColor,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Inversion bubble for NOT, NAND, NOR, XNOR
        if (isNot || isNand || isNor || isXnor) {
            val bubbleRadius = 4.dp.toPx()
            val bubbleCenterX = gateRight + bubbleRadius
            drawCircle(
                color = Color(0xFF070B14),
                radius = bubbleRadius,
                center = Offset(bubbleCenterX, gateCenterY)
            )
            drawCircle(
                color = gateColor,
                radius = bubbleRadius,
                center = Offset(bubbleCenterX, gateCenterY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

// =================================================================
// 3. ADVANCED FORMULAS & LAWS
// =================================================================

@Composable
fun AdvancedFormulasSection() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FormulaCard(
            title = "Ohm's Law & Electric Power",
            equation = "V = I × R   •   P = V × I = I²R = V² / R",
            summary = "Fundamental electrical relationship governing voltage (V), current (I), resistance (R), and dissipated power (P)."
        )

        FormulaCard(
            title = "Voltage Divider Rule",
            equation = "V_out = V_in × [ R2 / (R1 + R2) ]",
            summary = "Produces an output voltage that is a fraction of its input. Critical for sensor biasing and logic level shifting."
        )

        FormulaCard(
            title = "Kirchhoff's Current Law (KCL)",
            equation = "Σ I_entering = Σ I_leaving",
            summary = "The total current entering a junction or node equals the total current leaving that junction (conservation of electric charge)."
        )

        FormulaCard(
            title = "Kirchhoff's Voltage Law (KVL)",
            equation = "Σ V_loop = 0",
            summary = "The sum of all electrical potential differences (voltages) around any closed loop is zero (conservation of energy)."
        )

        FormulaCard(
            title = "Inverting Operational Amplifier Gain",
            equation = "V_out = - (R_feedback / R_in) × V_in",
            summary = "Negative closed-loop gain with 180° phase inversion. Virtual ground at inverting terminal."
        )

        FormulaCard(
            title = "Non-Inverting Op-Amp Gain",
            equation = "V_out = (1 + R_feedback / R_in) × V_in",
            summary = "Positive closed-loop gain with zero phase inversion. Extremely high input impedance."
        )

        FormulaCard(
            title = "LC Resonance Frequency (Tank Circuit)",
            equation = "f_0 = 1 / [ 2π × √(L × C) ]",
            summary = "The resonant frequency where inductive reactance XL equals capacitive reactance XC in RF filters and oscillators."
        )
    }
}

@Composable
fun FormulaCard(title: String, equation: String, summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceCard),
        border = BorderStroke(1.dp, ThemeOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ThemeSurfaceElevated,
                border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = equation,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ThemePrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
