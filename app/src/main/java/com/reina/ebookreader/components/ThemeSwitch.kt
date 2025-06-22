package com.reina.ebookreader.components

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val TAG = "ThemeSwitch"

/**
 * Кнопка переключения темы
 *
 * Упрощённый компонент для смены темы — по нажатию на иконку
 * происходит переключение между светлой и тёмной темой.
 *
 * @param isDarkTheme Текущий режим: тёмная тема или нет
 * @param onToggleDarkMode Колбэк для переключения темы
 */
@Composable
fun ThemeSwitch(
    isDarkTheme: Boolean,
    followSystem: Boolean,
    onToggleDarkMode: () -> Unit,
    onToggleFollowSystem: () -> Unit
) {
    Log.d(TAG, "ThemeSwitch: isDarkTheme=$isDarkTheme, followSystem=$followSystem")

    // Упрощённая кнопка: при нажатии переключает тему
    IconButton(
        onClick = { 
            Log.d(TAG, "ThemeSwitch: нажата кнопка переключения темы")
            onToggleDarkMode() 
        }
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
            contentDescription = if (isDarkTheme) "Переключить на светлую тему"
            else "Переключить на тёмную тему"
        )
    }
} 
