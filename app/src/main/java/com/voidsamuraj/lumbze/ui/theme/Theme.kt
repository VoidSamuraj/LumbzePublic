package com.voidsamuraj.lumbze.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.voidsamuraj.lumbze.R

private val DarkColorPalette = darkColors(
    primary = trunk2,
    primaryVariant = trunk1,
    secondary = Teal200
)
val mazeFont= FontFamily(
    //  Font(R.font.frederickathe_great_regular)
    Font(R.font.oleo_script_swash_caps_regular)
)
private val LightColorPalette = lightColors(
    primary = menuFrontColor,
    primaryVariant = trunk1,
    secondary = Teal200

    /* Other default colors to override
background = Color.White,
surface = Color.White,
onPrimary = Color.White,
onSecondary = Color.Black,
onBackground = Color.Black,
onSurface = Color.Black,
*/
)

@Composable
fun LumbzeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}