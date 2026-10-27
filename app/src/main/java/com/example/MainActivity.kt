package com.example

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Palette
import androidx.navigation.navArgument
import com.example.data.UserSettings
import com.example.ui.components.ThemeSelectorDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        UserSettings.init(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    var selectedCalculatorTool by remember { mutableIntStateOf(0) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    DisposableEffect(UserSettings.keepScreenOn) {
        val activity = context as? Activity
        if (UserSettings.keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { }
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate("settings") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val isStandaloneRoute = currentRoute.startsWith("feature_")

    val standaloneFeatureKey = remember(currentRoute, navBackStackEntry) {
        when {
            currentRoute.startsWith("feature_calc") -> {
                val toolIndex = navBackStackEntry?.arguments?.getInt("toolIndex") ?: 0
                "calc_$toolIndex"
            }
            currentRoute.startsWith("feature_ref") -> {
                val sectionIndex = navBackStackEntry?.arguments?.getInt("sectionIndex") ?: 0
                "ref_$sectionIndex"
            }
            currentRoute.startsWith("feature_components") -> "components"
            else -> ""
        }
    }

    AppBackgroundContainer(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (!isStandaloneRoute) {
                TopLabHeader(
                    onOpenThemeDialog = { showThemeDialog = true },
                    onNavigateToSettings = navigateToSettings
                )
            } else {
                StandaloneTopBar(
                    featureKey = standaloneFeatureKey,
                    onBackToHome = {
                        navController.popBackStack()
                    },
                    onOpenThemeDialog = { showThemeDialog = true },
                    onNavigateToSettings = navigateToSettings
                )
            }
        },
        bottomBar = {
            // High-tech docked navigation bar for main hubs
            if (!isStandaloneRoute) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    color = ThemeSurface,
                    tonalElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.height(68.dp)
                    ) {
                        val currentDestination = navBackStackEntry?.destination

                        // 1. HOME TAB (replaces old Library page)
                        val isHomeSelected = currentDestination?.hierarchy?.any { it.route == "home" } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home - All Features"
                                )
                            },
                            label = {
                                Text(
                                    "Home",
                                    fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isHomeSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ThemePrimary,
                                selectedTextColor = ThemePrimary,
                                indicatorColor = ThemePrimary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_home"),
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // 2. CALCULATORS TAB
                        val isCalcSelected = currentDestination?.hierarchy?.any { it.route == "calculators" } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (isCalcSelected) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                                    contentDescription = "Calculators & Testers"
                                )
                            },
                            label = {
                                Text(
                                    "Calculators",
                                    fontWeight = if (isCalcSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isCalcSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ThemePrimary,
                                selectedTextColor = ThemePrimary,
                                indicatorColor = ThemePrimary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_calculators"),
                            onClick = {
                                navController.navigate("calculators") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // 3. REFERENCE TAB
                        val isRefSelected = currentDestination?.hierarchy?.any { it.route == "reference" } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (isRefSelected) Icons.Filled.DeveloperBoard else Icons.Outlined.DeveloperBoard,
                                    contentDescription = "Pinouts & Logic"
                                )
                            },
                            label = {
                                Text(
                                    "Pinouts & Logic",
                                    fontWeight = if (isRefSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isRefSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ThemePrimary,
                                selectedTextColor = ThemePrimary,
                                indicatorColor = ThemePrimary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_reference"),
                            onClick = {
                                navController.navigate("reference") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // 4. COMPONENTS TAB
                        val isComponentsSelected = currentDestination?.hierarchy?.any { it.route == "components" } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (isComponentsSelected) Icons.Filled.Widgets else Icons.Outlined.Widgets,
                                    contentDescription = "Components Catalog"
                                )
                            },
                            label = {
                                Text(
                                    "Components",
                                    fontWeight = if (isComponentsSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isComponentsSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ThemePrimary,
                                selectedTextColor = ThemePrimary,
                                indicatorColor = ThemePrimary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_components"),
                            onClick = {
                                navController.navigate("components") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // 5. SETTINGS TAB
                        val isSettingsSelected = currentDestination?.hierarchy?.any { it.route == "settings" } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (isSettingsSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings & Preferences"
                                )
                            },
                            label = {
                                Text(
                                    "Settings",
                                    fontWeight = if (isSettingsSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isSettingsSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ThemePrimary,
                                selectedTextColor = ThemePrimary,
                                indicatorColor = ThemePrimary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_settings"),
                            onClick = {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(100)) },
            exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(100)) },
            popEnterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(100)) },
            popExitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(100)) }
        ) {
            // 1. HOME SCREEN - Central Hub listing all features by name
            composable("home") {
                HomeScreen(
                    onNavigateToFeature = { targetRoute ->
                        navController.navigate(targetRoute)
                    },
                    onOpenThemeDialog = { showThemeDialog = true }
                )
            }

            // 2. DEDICATED STANDALONE FEATURE VIEWS ("ami je feature a click korbo sei feature open hobe sudu")
            composable(
                route = "feature_calc/{toolIndex}",
                arguments = listOf(navArgument("toolIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val toolIndex = backStackEntry.arguments?.getInt("toolIndex") ?: 0
                StandaloneFeatureScreen(
                    featureKey = "calc_$toolIndex",
                    onBackToHome = {
                        navController.popBackStack()
                    },
                    onOpenThemeDialog = { showThemeDialog = true }
                )
            }

            composable(
                route = "feature_ref/{sectionIndex}",
                arguments = listOf(navArgument("sectionIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val sectionIndex = backStackEntry.arguments?.getInt("sectionIndex") ?: 0
                StandaloneFeatureScreen(
                    featureKey = "ref_$sectionIndex",
                    onBackToHome = {
                        navController.popBackStack()
                    },
                    onOpenThemeDialog = { showThemeDialog = true }
                )
            }

            composable("feature_components") {
                StandaloneFeatureScreen(
                    featureKey = "components",
                    onBackToHome = {
                        navController.popBackStack()
                    },
                    onOpenThemeDialog = { showThemeDialog = true }
                )
            }

            // 3. FULL HUB DESTINATIONS
            composable("calculators") {
                CalculatorScreen(
                    initialToolIndex = selectedCalculatorTool,
                    onToolChange = { selectedCalculatorTool = it }
                )
            }

            composable("reference") {
                ReferenceScreen()
            }

            composable("components") {
                SearchScreen(
                    onNavigateToCalculator = { toolIndex ->
                        selectedCalculatorTool = toolIndex
                        navController.navigate("calculators") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // 4. SETTINGS HUB
            composable("settings") {
                SettingsScreen(
                    onOpenThemeDialog = { showThemeDialog = true }
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectorDialog(onDismiss = { showThemeDialog = false })
    }
    }
}

@Composable
fun TopLabHeader(
    onOpenThemeDialog: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val chipGradient = remember(ThemeManager.currentTheme) {
        Brush.linearGradient(
            listOf(ThemePrimary.copy(alpha = 0.35f), ThemePrimaryContainer)
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (ThemeManager.currentPalette.isLiquidGlass) ThemeSurface.copy(alpha = 0.85f) else ThemeSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Microchip glow badge + Title branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(chipGradient)
                        .border(1.dp, ThemePrimary.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Memory,
                        contentDescription = "ElectroMaster",
                        tint = ThemePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ELECTRO MASTER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ThemePrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = ThemePrimary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Engineering Suite & Lab Toolkit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Quick-access Theme Palette and Settings IconButtons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onOpenThemeDialog,
                    modifier = Modifier.testTag("header_theme_button")
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ThemePrimary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Palette,
                                contentDescription = "Themes",
                                tint = ThemePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.testTag("header_settings_button")
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ThemeSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
