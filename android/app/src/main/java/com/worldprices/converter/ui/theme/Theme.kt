package com.worldprices.converter.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.worldprices.converter.data.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = OnPrimaryContainerLight,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

@Composable
fun WorldPricesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val targetScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val animSpec = tween<Color>(durationMillis = 220)
    val bg by animateColorAsState(targetScheme.background, animSpec, label = "bg")
    val surface by animateColorAsState(targetScheme.surface, animSpec, label = "surface")
    val surfaceVariant by animateColorAsState(targetScheme.surfaceVariant, animSpec, label = "surfaceVariant")
    val onBg by animateColorAsState(targetScheme.onBackground, animSpec, label = "onBg")
    val onSurface by animateColorAsState(targetScheme.onSurface, animSpec, label = "onSurface")
    val onSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, animSpec, label = "onSurfaceVariant")

    val colorScheme = targetScheme.copy(
        background = bg,
        surface = surface,
        surfaceVariant = surfaceVariant,
        onBackground = onBg,
        onSurface = onSurface,
        onSurfaceVariant = onSurfaceVariant
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
