package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 4 Distinct App Themes:
 * 1. DARK: OLED Cyber-Lab (Pitch Obsidian & Electric Cyan)
 * 2. BLACK_WHITE: PCB Gold & Matte Noir (ENIG Gold Traces & Carbon Black)
 * 3. LIQUID_GLASS: Spatial Frosted Glass (Deep Aurora Mesh & Translucent Acrylic)
 * 4. WHITE: Alabaster Pro Studio (Warm Daylight Paper & Precision Cobalt)
 */
enum class AppTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val isDark: Boolean
) {
    DARK(
        id = "dark",
        title = "OLED Cyber-Lab",
        subtitle = "Pitch Obsidian & Electric Cyan",
        isDark = true
    ),
    BLACK_WHITE(
        id = "black_white",
        title = "PCB Gold & Matte Noir",
        subtitle = "ENIG Gold Traces & Carbon Black",
        isDark = true
    ),
    LIQUID_GLASS(
        id = "liquid_glass",
        title = "Spatial Frosted Glass",
        subtitle = "Deep Aurora Mesh & Translucent Acrylic",
        isDark = true
    ),
    WHITE(
        id = "white",
        title = "Alabaster Pro Studio",
        subtitle = "Warm Daylight Paper & Precision Cobalt",
        isDark = false
    )
}

@Immutable
data class ElectroPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val onSurfaceMuted: Color,
    val primary: Color,
    val primaryGlow: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val outline: Color,
    val outlineBright: Color,
    val navigationBar: Color,
    val accentGreen: Color,
    val accentGreenGlow: Color,
    val accentAmber: Color,
    val accentRed: Color,
    val accentPurple: Color,
    val accentGold: Color,
    val accentLead: Color,
    val accentCyan: Color,
    val isLiquidGlass: Boolean = false,
    val cardAlpha: Float = 1.0f
)

// 1. OLED Cyber-Lab (Pitch Obsidian & Electric Cyan)
val DarkPalette = ElectroPalette(
    background = Color(0xFF050811), // True OLED pitch obsidian
    surface = Color(0xFF0B101D),
    surfaceVariant = Color(0xFF131C2E),
    surfaceCard = Color(0xFF0D1322), // Layered rich slate card
    surfaceElevated = Color(0xFF151E32), // Elevated surface
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    onSurfaceMuted = Color(0xFF64748B),
    primary = Color(0xFF00E5FF), // Crisp neon cyan primary
    primaryGlow = Color(0xFF38F9D7),
    primaryContainer = Color(0xFF0A2E3D),
    onPrimaryContainer = Color(0xFFA5F3FC),
    secondary = Color(0xFF818CF8), // Electric indigo secondary
    secondaryContainer = Color(0xFF1E224D),
    onSecondaryContainer = Color(0xFFE0E7FF),
    outline = Color(0xFF1E2B45), // Subtle specular border outline
    outlineBright = Color(0xFF2E4166),
    navigationBar = Color(0xFF050811),
    accentGreen = Color(0xFF10B981),
    accentGreenGlow = Color(0xFF34D399),
    accentAmber = Color(0xFFF59E0B),
    accentRed = Color(0xFFEF4444),
    accentPurple = Color(0xFFA855F7),
    accentGold = Color(0xFFFBBF24),
    accentLead = Color(0xFF94A3B8),
    accentCyan = Color(0xFF00E5FF),
    isLiquidGlass = false,
    cardAlpha = 1.0f
)

// 2. PCB Gold & Matte Noir (ENIG Gold Traces & Carbon Black)
val BlackWhitePalette = ElectroPalette(
    background = Color(0xFF08080A), // Carbon black PCB substrate
    surface = Color(0xFF101014), // Dark graphite surface
    surfaceVariant = Color(0xFF18191E),
    surfaceCard = Color(0xFF121316), // Matte graphite card
    surfaceElevated = Color(0xFF1B1C22),
    onBackground = Color(0xFFFDFBF7),
    onSurface = Color(0xFFF5F5F0),
    onSurfaceVariant = Color(0xFFA8A29E), // Warm titanium slate
    onSurfaceMuted = Color(0xFF78716C),
    primary = Color(0xFFF5B041), // Warm metallic ENIG gold
    primaryGlow = Color(0xFFFAD7A0),
    primaryContainer = Color(0xFF332510),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFE5E7EB), // Champagne secondary
    secondaryContainer = Color(0xFF26262B),
    onSecondaryContainer = Color(0xFFF3F4F6),
    outline = Color(0xFF2A261D), // Dark bronze border
    outlineBright = Color(0xFF4A4333),
    navigationBar = Color(0xFF08080A),
    accentGreen = Color(0xFF10B981), // Functional semantic green for pinouts
    accentGreenGlow = Color(0xFF34D399),
    accentAmber = Color(0xFFF59E0B),
    accentRed = Color(0xFFF43F5E), // Functional semantic red
    accentPurple = Color(0xFFA855F7),
    accentGold = Color(0xFFF5B041),
    accentLead = Color(0xFFA8A29E),
    accentCyan = Color(0xFF38BDF8),
    isLiquidGlass = false,
    cardAlpha = 1.0f
)

// 3. Spatial Frosted Glass (Deep Aurora Mesh & Translucent Acrylic)
val LiquidGlassPalette = ElectroPalette(
    background = Color(0xFF060919), // Deep cosmic indigo
    surface = Color(0xDD0D132D), // Frosted acrylic surface
    surfaceVariant = Color(0xCC182046),
    surfaceCard = Color(0xB8111A3A), // Translucent glass card (cardAlpha = 0.72f)
    surfaceElevated = Color(0xCC1F2958),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFF3F6FF),
    onSurfaceVariant = Color(0xFFB3C0F0),
    onSurfaceMuted = Color(0xFF7B8BC0),
    primary = Color(0xFF38F9D7), // Luminous aqua glow
    primaryGlow = Color(0xFF4EFAFF),
    primaryContainer = Color(0x400284C7),
    onPrimaryContainer = Color(0xFFE0F7FF),
    secondary = Color(0xFFC084FC), // Violet-orchid secondary
    secondaryContainer = Color(0x387C3AED),
    onSecondaryContainer = Color(0xFFF3E8FF),
    outline = Color(0x6638F9D7), // Bright refractive cyan edge
    outlineBright = Color(0x99C084FC), // Prismatic orchid highlight
    navigationBar = Color(0xF0060919),
    accentGreen = Color(0xFF00FFB2),
    accentGreenGlow = Color(0xFF5CFFCD),
    accentAmber = Color(0xFFFFBA08),
    accentRed = Color(0xFFFF3366),
    accentPurple = Color(0xFFC084FC),
    accentGold = Color(0xFFFFD166),
    accentLead = Color(0xFF94A3B8),
    accentCyan = Color(0xFF38F9D7),
    isLiquidGlass = true,
    cardAlpha = 0.72f
)

// 4. Alabaster Pro Studio (Warm Daylight Paper & Precision Cobalt)
val WhitePalette = ElectroPalette(
    background = Color(0xFFF4F6FB), // Soft warm alabaster
    surface = Color(0xFFFFFFFF), // Pure white
    surfaceVariant = Color(0xFFEDF1F7),
    surfaceCard = Color(0xFFFFFFFF), // Pure white elevated card
    surfaceElevated = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A), // High-contrast slate-900
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF475569),
    onSurfaceMuted = Color(0xFF64748B),
    primary = Color(0xFF2563EB), // Deep cobalt primary
    primaryGlow = Color(0xFF3B82F6),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1D4ED8),
    secondary = Color(0xFF4F46E5), // Indigo secondary
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF3730A3),
    outline = Color(0xFFE2E8F0), // Crisp slate-200 border
    outlineBright = Color(0xFFCBD5E1),
    navigationBar = Color(0xFFFFFFFF),
    accentGreen = Color(0xFF059669),
    accentGreenGlow = Color(0xFF10B981),
    accentAmber = Color(0xFFD97706),
    accentRed = Color(0xFFDC2626),
    accentPurple = Color(0xFF7C3AED),
    accentGold = Color(0xFFB45309),
    accentLead = Color(0xFF64748B),
    accentCyan = Color(0xFF0891B2),
    isLiquidGlass = false,
    cardAlpha = 1.0f
)

/**
 * Global Theme Manager with instant reactive state and SharedPreferences persistence.
 */
object ThemeManager {
    var currentTheme by mutableStateOf(AppTheme.DARK)
        private set

    var currentPalette by mutableStateOf(DarkPalette)
        private set

    private fun resolvePalette(theme: AppTheme): ElectroPalette {
        return when (theme) {
            AppTheme.DARK -> DarkPalette
            AppTheme.BLACK_WHITE -> BlackWhitePalette
            AppTheme.LIQUID_GLASS -> LiquidGlassPalette
            AppTheme.WHITE -> WhitePalette
        }
    }

    fun setTheme(theme: AppTheme, context: Context? = null) {
        currentTheme = theme
        currentPalette = resolvePalette(theme)
        if (context != null) {
            try {
                context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("selected_theme", theme.name)
                    .apply()
            } catch (e: Exception) {
                // Ignore persistence errors gracefully
            }
        }
    }

    fun init(context: Context) {
        try {
            val saved = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)
                .getString("selected_theme", AppTheme.DARK.name)
            currentTheme = try {
                AppTheme.valueOf(saved ?: AppTheme.DARK.name)
            } catch (e: Exception) {
                AppTheme.DARK
            }
        } catch (e: Exception) {
            currentTheme = AppTheme.DARK
        }
        currentPalette = resolvePalette(currentTheme)
    }
}

val LocalAppTheme = staticCompositionLocalOf { AppTheme.DARK }
val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

// Dynamic reactive theme color getters accessed across all Compose screens
val ThemeBackground: Color get() = ThemeManager.currentPalette.background
val ThemeSurface: Color get() = ThemeManager.currentPalette.surface
val ThemeSurfaceVariant: Color get() = ThemeManager.currentPalette.surfaceVariant
val ThemeSurfaceCard: Color get() = ThemeManager.currentPalette.surfaceCard
val ThemeSurfaceElevated: Color get() = ThemeManager.currentPalette.surfaceElevated
val ThemeOnBackground: Color get() = ThemeManager.currentPalette.onBackground
val ThemeOnSurface: Color get() = ThemeManager.currentPalette.onSurface
val ThemeOnSurfaceVariant: Color get() = ThemeManager.currentPalette.onSurfaceVariant
val ThemeOnSurfaceMuted: Color get() = ThemeManager.currentPalette.onSurfaceMuted
val ThemePrimary: Color get() = ThemeManager.currentPalette.primary
val ThemePrimaryGlow: Color get() = ThemeManager.currentPalette.primaryGlow
val ThemePrimaryContainer: Color get() = ThemeManager.currentPalette.primaryContainer
val ThemeOnPrimaryContainer: Color get() = ThemeManager.currentPalette.onPrimaryContainer
val ThemeSecondary: Color get() = ThemeManager.currentPalette.secondary
val ThemeSecondaryContainer: Color get() = ThemeManager.currentPalette.secondaryContainer
val ThemeOnSecondaryContainer: Color get() = ThemeManager.currentPalette.onSecondaryContainer
val ThemeOutline: Color get() = ThemeManager.currentPalette.outline
val ThemeOutlineBright: Color get() = ThemeManager.currentPalette.outlineBright
val ThemeNavigationBar: Color get() = ThemeManager.currentPalette.navigationBar
val ThemeAccentGreen: Color get() = ThemeManager.currentPalette.accentGreen
val ThemeAccentGreenGlow: Color get() = ThemeManager.currentPalette.accentGreenGlow
val ThemeAccentAmber: Color get() = ThemeManager.currentPalette.accentAmber
val ThemeAccentRed: Color get() = ThemeManager.currentPalette.accentRed
val ThemeAccentPurple: Color get() = ThemeManager.currentPalette.accentPurple
val ThemeAccentGold: Color get() = ThemeManager.currentPalette.accentGold
val ThemeAccentLead: Color get() = ThemeManager.currentPalette.accentLead
val ThemeAccentCyan: Color get() = ThemeManager.currentPalette.accentCyan
