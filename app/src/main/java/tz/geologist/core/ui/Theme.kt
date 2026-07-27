package tz.geologist.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette inayolingana na blueprint (kijani cha jiolojia)
private val Accent = Color(0xFF3D6B39)
private val Ink = Color(0xFF2D4A2B)
private val Bg = Color(0xFFFAF9F6)

private val LightColors = lightColorScheme(
    primary = Accent, onPrimary = Color.White,
    secondary = Ink, background = Bg, surface = Color(0xFFF0EFE8)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA4AC86), secondary = Color(0xFFE4E8D8)
)

@Composable
fun AiGeologistTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
