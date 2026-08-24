package com.example.alhaja.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF684274),
    onPrimary = Color.White,
    secondary = Color(0xFF8A6A1E),
    background = Color(0xFFFFF8FC),
    surface = Color(0xFFFFF8FC),
    surfaceVariant = Color(0xFFF1E6F1)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFD9B8E5),
    secondary = Color(0xFFE7C363),
    background = Color(0xFF171218),
    surface = Color(0xFF171218),
    surfaceVariant = Color(0xFF342C35)
)

@Composable
fun AlhajaTheme(
    modoOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
        typography = MaterialTheme.typography,
        content = content
    )
}
