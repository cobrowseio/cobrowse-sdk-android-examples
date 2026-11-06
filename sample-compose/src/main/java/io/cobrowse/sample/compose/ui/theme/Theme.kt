package io.cobrowse.sample.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The Material 3 baseline colors, with the Cobrowse.io purple as the light primary color
private val LightColorScheme = lightColorScheme(primary = PrimaryColor)

private val DarkColorScheme = darkColorScheme()

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
