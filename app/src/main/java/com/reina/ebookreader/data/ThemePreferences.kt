package com.reina.ebookreader.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "ThemePreferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {

    // Определяем ключи для настроек
    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
    }

    // Получаем текущий статус тёмной темы
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val value = preferences[IS_DARK_MODE] ?: false
            Log.d(TAG, "Чтение настройки тёмного режима: $value")
            value
        }

    // Получаем настройку «Следовать системным настройкам»
    val followSystem: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val value = preferences[FOLLOW_SYSTEM] ?: true
            Log.d(TAG, "Чтение настройки следования системным настройкам: $value")
            value
        }

    // Устанавливаем тёмный режим
    suspend fun setDarkMode(isDark: Boolean) {
        Log.d(TAG, "Установка тёмного режима: $isDark")
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    // Устанавливаем следование системным настройкам
    suspend fun setFollowSystem(follow: Boolean) {
        Log.d(TAG, "Установка следования системным настройкам: $follow")
        context.dataStore.edit { preferences ->
            preferences[FOLLOW_SYSTEM] = follow
        }
    }
}
