package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val BocchiPink = Color(0xFFEC407A)
val BocchiHotPink = Color(0xFFD65484)
val NijikaYellow = Color(0xFFF9B000)
val RyoBlue = Color(0xFF99D6F1)
val KitaRed = Color(0xFFCE2525)
val StarryDark = Color(0xFF141414)
val StarrySurface = Color(0xFF1E1E22)
val ProducerPurple = Color(0xFFC559FF)

val AaPixelFontFamily = FontFamily(Font(R.font.aa_pixel, FontWeight.Normal))
val BrushScriptFontFamily = FontFamily(Font(R.font.brush_script_mt_italic, FontWeight.Normal))
val NoteScriptFontFamily = FontFamily(Font(R.font.note_script_semibold, FontWeight.Normal))
val NotoSansScFontFamily = FontFamily(Font(R.font.noto_sans_sc, FontWeight.Normal))
val NotoSerifScFontFamily = FontFamily(Font(R.font.noto_serif_sc, FontWeight.Normal))

private val BocchiDarkColorScheme = darkColorScheme(
    primary = BocchiPink,
    onPrimary = Color.White,
    secondary = NijikaYellow,
    onSecondary = Color.Black,
    tertiary = RyoBlue,
    onTertiary = Color.Black,
    background = StarryDark,
    onBackground = Color.White,
    surface = StarrySurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A30),
    onSurfaceVariant = Color(0xFFD0D0D8)
)

val BocchiTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun BocchiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BocchiDarkColorScheme,
        typography = BocchiTypography,
        content = content
    )
}
