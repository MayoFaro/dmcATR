package com.gap.dmcgap.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Icon


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = md_theme_light_background
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun DmcGapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ThemedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor    = Color(0xFFF4A261)
    val fillColor      = Color(0xFFE07A5F)
    val bgColor        = Color(0xFFFFF0E6)
    val checkmarkColor = Color.White

    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier
            // on augmente la taille de la case
            .size(12.dp)
            // et on scale le dessin interne (cocher/décocher)
            .scale(1.0f)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (checked) fillColor else bgColor,
                shape = RoundedCornerShape(4.dp)
            ),
        colors = CheckboxDefaults.colors(
            checkedColor           = fillColor,
            uncheckedColor         = bgColor,
            checkmarkColor         = checkmarkColor,
            disabledCheckedColor   = fillColor.copy(alpha = 0.5f),
            disabledUncheckedColor = bgColor.copy(alpha = 0.5f)
        )
    )
}
@Composable
fun OverflowingCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    // ici on ne fournit QUE des valeurs par défaut
    boxColorChecked: Color    = Color(0xFFFFE0B2),
    boxColorUnchecked: Color  = Color.Transparent,
    borderColor: Color        = Color(0xFFCC8E64),
    tickColor: Color          = Color(0xFF6D4C41),
    boxSize: Dp               = 24.dp,
    tickSize: Dp              = 32.dp
) {
    val boxColor = if (checked) boxColorChecked else boxColorUnchecked

    Box(
        modifier = modifier
            .size(boxSize)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        // fond + bordure
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(boxColor, shape = RoundedCornerShape(4.dp))
                .border(3.dp, borderColor, shape = RoundedCornerShape(4.dp))
        )
        // tic
        if (checked) {
            Icon(
                imageVector    = Icons.Default.Check,
                contentDescription = null,
                tint           = tickColor,
                modifier       = Modifier.size(tickSize)
            )
        }
    }
}
