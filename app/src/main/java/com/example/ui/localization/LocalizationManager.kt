package com.example.ui.localization

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages runtime and persistent language selection for the Nirzor KingMaker application.
 * Persists the user preference in SharedPreferences, defaulting to Bengali (BN).
 */
class LocalizationManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(loadLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private fun loadLanguage(): AppLanguage {
        val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.DEFAULT.code)
        return AppLanguage.fromCode(savedCode)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _currentLanguage.value = language
    }

    companion object {
        private const val PREFS_NAME = "kingmaker_localization_prefs"
        private const val KEY_LANGUAGE = "selected_app_language"

        @Volatile
        private var instance: LocalizationManager? = null

        fun getInstance(context: Context): LocalizationManager {
            return instance ?: synchronized(this) {
                instance ?: LocalizationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
