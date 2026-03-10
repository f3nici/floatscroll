package com.floatscroll

import android.content.Context
import android.content.SharedPreferences

object SettingsStore {

    private const val PREFS_NAME = "floatscroll_prefs"
    private const val KEY_SCROLL_DISTANCE = "scroll_distance"
    private const val KEY_BUTTON_OPACITY = "button_opacity"
    private const val KEY_BUTTON_SIZE = "button_size"

    private const val DEFAULT_SCROLL_DISTANCE = 600
    private const val DEFAULT_BUTTON_OPACITY = 0.7f
    private const val DEFAULT_BUTTON_SIZE = "medium"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var scrollDistance: Int
        get() = prefs.getInt(KEY_SCROLL_DISTANCE, DEFAULT_SCROLL_DISTANCE)
        set(value) = prefs.edit().putInt(KEY_SCROLL_DISTANCE, value).apply()

    var buttonOpacity: Float
        get() = prefs.getFloat(KEY_BUTTON_OPACITY, DEFAULT_BUTTON_OPACITY)
        set(value) = prefs.edit().putFloat(KEY_BUTTON_OPACITY, value).apply()

    var buttonSize: String
        get() = prefs.getString(KEY_BUTTON_SIZE, DEFAULT_BUTTON_SIZE) ?: DEFAULT_BUTTON_SIZE
        set(value) = prefs.edit().putString(KEY_BUTTON_SIZE, value).apply()

    fun getButtonSizeDp(): Int = when (buttonSize) {
        "small" -> 40
        "large" -> 72
        else -> 56
    }
}
