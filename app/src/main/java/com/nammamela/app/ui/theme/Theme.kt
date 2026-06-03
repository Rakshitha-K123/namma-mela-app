package com.nammamela.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NammaMelaLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = Color(0xFF431407),
    secondary = Color(0xFFBE123C),
    onSecondary = Color.White,
    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF1F2937),
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563)
)

@Composable
fun NammaMelaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NammaMelaLightColors,
        typography = Typography,
        content = content
    )
}
