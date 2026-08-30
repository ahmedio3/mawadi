package com.mymaterials.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// IBM Plex Sans Arabic - نستخدم System Font كـ fallback
// لإضافة الخط فعليا ضع ملفات ttf في res/font وبدل FontFamily.Default
val IbmPlexFamily = FontFamily.Default

val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = IbmPlexFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    headlineMedium = TextStyle(fontFamily = IbmPlexFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = IbmPlexFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = IbmPlexFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = IbmPlexFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = IbmPlexFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = IbmPlexFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
)
