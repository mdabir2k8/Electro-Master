package com.example.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UserSettings {
    private const val PREFS_NAME = "electro_master_settings"

    var isInitialized by mutableStateOf(false)
        private set

    var keepScreenOn by mutableStateOf(false)
        private set

    var hapticFeedback by mutableStateOf(true)
        private set

    var decimalPrecision by mutableIntStateOf(2)
        private set

    var tempUnit by mutableStateOf("°C")
        private set

    var freqUnit by mutableStateOf("kHz")
        private set

    var defaultResistorTolerance by mutableStateOf("5% (Gold)")
        private set

    var defaultBoard by mutableStateOf("uno")
        private set

    var showFormulasAlways by mutableStateOf(true)
        private set

    // Home Customization Settings
    val defaultPinnedIds = setOf(
        "resistor_color",
        "microcontroller_pinouts",
        "logic_simulator",
        "component_catalog"
    )

    var pinnedFeatureIds by mutableStateOf<Set<String>>(defaultPinnedIds)
        private set

    var isCompactGridView by mutableStateOf(false)
        private set

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            keepScreenOn = prefs.getBoolean("keep_screen_on", false)
            hapticFeedback = prefs.getBoolean("haptic_feedback", true)
            decimalPrecision = prefs.getInt("decimal_precision", 2)
            tempUnit = prefs.getString("temp_unit", "°C") ?: "°C"
            freqUnit = prefs.getString("freq_unit", "kHz") ?: "kHz"
            defaultResistorTolerance = prefs.getString("resistor_tolerance", "5% (Gold)") ?: "5% (Gold)"
            defaultBoard = prefs.getString("default_board", "uno") ?: "uno"
            showFormulasAlways = prefs.getBoolean("show_formulas_always", true)
            
            val savedPins = prefs.getStringSet("pinned_feature_ids", null)
            pinnedFeatureIds = if (savedPins != null && savedPins.isNotEmpty()) savedPins else defaultPinnedIds
            isCompactGridView = prefs.getBoolean("compact_grid_view", false)
            
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
            isInitialized = true
        }
    }

    fun setKeepScreenOn(enabled: Boolean, context: Context? = null) {
        keepScreenOn = enabled
        saveBoolean("keep_screen_on", enabled, context)
    }

    fun setHapticFeedback(enabled: Boolean, context: Context? = null) {
        hapticFeedback = enabled
        saveBoolean("haptic_feedback", enabled, context)
    }

    fun setDecimalPrecision(precision: Int, context: Context? = null) {
        decimalPrecision = precision.coerceIn(1, 6)
        saveInt("decimal_precision", decimalPrecision, context)
    }

    fun setTempUnit(unit: String, context: Context? = null) {
        tempUnit = unit
        saveString("temp_unit", unit, context)
    }

    fun setFreqUnit(unit: String, context: Context? = null) {
        freqUnit = unit
        saveString("freq_unit", unit, context)
    }

    fun setDefaultResistorTolerance(tolerance: String, context: Context? = null) {
        defaultResistorTolerance = tolerance
        saveString("resistor_tolerance", tolerance, context)
    }

    fun setDefaultBoard(boardId: String, context: Context? = null) {
        defaultBoard = boardId
        saveString("default_board", boardId, context)
    }

    fun setShowFormulasAlways(enabled: Boolean, context: Context? = null) {
        showFormulasAlways = enabled
        saveBoolean("show_formulas_always", enabled, context)
    }

    fun setCompactGridView(enabled: Boolean, context: Context? = null) {
        isCompactGridView = enabled
        saveBoolean("compact_grid_view", enabled, context)
    }

    fun togglePinnedFeature(id: String, context: Context? = null) {
        val current = pinnedFeatureIds.toMutableSet()
        if (current.contains(id)) {
            // Keep at least 1 pinned
            if (current.size > 1) {
                current.remove(id)
            }
        } else {
            // Cap at 4 or 6 favorites
            if (current.size >= 4) {
                val oldest = current.first()
                current.remove(oldest)
            }
            current.add(id)
        }
        pinnedFeatureIds = current
        saveStringSet("pinned_feature_ids", current, context)
    }

    fun setPinnedFeatures(ids: Set<String>, context: Context? = null) {
        if (ids.isNotEmpty()) {
            pinnedFeatureIds = ids
            saveStringSet("pinned_feature_ids", ids, context)
        }
    }

    fun resetToDefaults(context: Context? = null) {
        keepScreenOn = false
        hapticFeedback = true
        decimalPrecision = 2
        tempUnit = "°C"
        freqUnit = "kHz"
        defaultResistorTolerance = "5% (Gold)"
        defaultBoard = "uno"
        showFormulasAlways = true
        pinnedFeatureIds = defaultPinnedIds
        isCompactGridView = false

        if (context != null) {
            try {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveBoolean(key: String, value: Boolean, context: Context?) {
        context?.let {
            try {
                it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(key, value)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveInt(key: String, value: Int, context: Context?) {
        context?.let {
            try {
                it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(key, value)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveString(key: String, value: String, context: Context?) {
        context?.let {
            try {
                it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(key, value)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveStringSet(key: String, value: Set<String>, context: Context?) {
        context?.let {
            try {
                it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putStringSet(key, value)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
