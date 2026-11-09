package io.cobrowse.sample.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The Material 3 baseline colors, with the Cobrowse.io purple as the primary color in both
// themes, like in the View-based sample
private val LightColorScheme = lightColorScheme(primary = PrimaryColor)

private val DarkColorScheme = darkColorScheme(primary = PrimaryColor, onPrimary = Color.White)

@Composable
fun SampleComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
