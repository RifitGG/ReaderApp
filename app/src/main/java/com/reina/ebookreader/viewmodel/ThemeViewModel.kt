package com.reina.ebookreader.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reina.ebookreader.data.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val TAG = "ThemeViewModel"

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application.applicationContext)

    // Использовать ли тёмную тему
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Следовать системным настройкам
    private val _followSystem = MutableStateFlow(true)
    val followSystem: StateFlow<Boolean> = _followSystem.asStateFlow()

    init {
        // Загружаем сохранённые настройки темы
        viewModelScope.launch {
            combine(
                themePreferences.isDarkMode,
                themePreferences.followSystem
            ) { isDark, followSys ->
                Pair(isDark, followSys)
            }.collect { (isDark, followSys) ->
                Log.d(TAG, "Инициализация настроек темы: isDark=$isDark, followSystem=$followSys")
                _isDarkTheme.value = isDark
                _followSystem.value = followSys
            }
        }
    }

    // Переключение тёмного/светлого режима
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_isDarkTheme.value
            Log.d(TAG, "Переключение тёмного режима: $newValue")
            _isDarkTheme.value = newValue
            themePreferences.setDarkMode(newValue)

            // Если пользователь вручную изменил тему, отключаем следование системе
            if (_followSystem.value) {
                Log.d(TAG, "Отключение следования системным настройкам")
                _followSystem.value = false
                themePreferences.setFollowSystem(false)
            }
        }
    }

    // Переключение следования системным настройкам
    fun toggleFollowSystem() {
        viewModelScope.launch {
            val newValue = !_followSystem.value
            Log.d(TAG, "Переключение следования системе: $newValue")
            _followSystem.value = newValue
            themePreferences.setFollowSystem(newValue)
        }
    }

    // Определяет, следует ли использовать тёмную тему
    fun shouldUseDarkTheme(isSystemInDarkMode: Boolean): Boolean {
        val result = if (_followSystem.value) {
            isSystemInDarkMode
        } else {
            _isDarkTheme.value
        }
        Log.d(TAG, "Результат выбора темы: followSystem=${_followSystem.value}, isSystemInDarkMode=$isSystemInDarkMode, isDarkTheme=${_isDarkTheme.value}, итог=$result")
        return result
    }
}
