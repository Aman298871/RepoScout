package com.example.reposcout.ui.theme

import androidx.compose.ui.graphics.Color

// High Density Theme - Light Color Palette
val HighDensityPrimaryLight = Color(0xFF004A77)
val HighDensityOnPrimaryLight = Color(0xFFFFFFFF)
val HighDensityPrimaryContainerLight = Color(0xFFDCE1FF)
val HighDensityOnPrimaryContainerLight = Color(0xFF001552)

val HighDensitySecondaryLight = Color(0xFF535F70)
val HighDensityOnSecondaryLight = Color(0xFFFFFFFF)
val HighDensitySecondaryContainerLight = Color(0xFFE0E2EC)
val HighDensityOnSecondaryContainerLight = Color(0xFF1B1B1F)

val HighDensityTertiaryLight = Color(0xFF6B5778)
val HighDensityOnTertiaryLight = Color(0xFFFFFFFF)
val HighDensityTertiaryContainerLight = Color(0xFFF3DAFF)
val HighDensityOnTertiaryContainerLight = Color(0xFF251431)

val HighDensityBackgroundLight = Color(0xFFFDFCFF)
val HighDensitySurfaceLight = Color(0xFFFFFFFF)
val HighDensitySurfaceVariantLight = Color(0xFFE0E2EC)
val HighDensityOutlineLight = Color(0xFFC4C6CF)
val HighDensityTextPrimaryLight = Color(0xFF001D36)
val HighDensityTextSecondaryLight = Color(0xFF44474E)
val HighDensityErrorLight = Color(0xFFBA1A1A)

// High Density Theme - Dark Color Palette
val HighDensityPrimaryDark = Color(0xFFA5C8FF)
val HighDensityOnPrimaryDark = Color(0xFF003153)
val HighDensityPrimaryContainerDark = Color(0xFF004A77)
val HighDensityOnPrimaryContainerDark = Color(0xFFDCE1FF)

val HighDensitySecondaryDark = Color(0xFFBCC7DB)
val HighDensityOnSecondaryDark = Color(0xFF253140)
val HighDensitySecondaryContainerDark = Color(0xFF3B4858)
val HighDensityOnSecondaryContainerDark = Color(0xFFD7E3F7)

val HighDensityTertiaryDark = Color(0xFFD6BEE4)
val HighDensityOnTertiaryDark = Color(0xFF3B2948)
val HighDensityTertiaryContainerDark = Color(0xFF523F5F)
val HighDensityOnTertiaryContainerDark = Color(0xFFF3DAFF)

val HighDensityBackgroundDark = Color(0xFF1A1C1E)
val HighDensitySurfaceDark = Color(0xFF1A1C1E)
val HighDensitySurfaceVariantDark = Color(0xFF44474E)
val HighDensityOutlineDark = Color(0xFF8E9099)
val HighDensityTextPrimaryDark = Color(0xFFE2E2E6)
val HighDensityTextSecondaryDark = Color(0xFFC4C6CF)
val HighDensityErrorDark = Color(0xFFFFB4AB)

// Language tag chip colors
val LanguageKotlin = Color(0xFFF18E33)
val LanguageJava = Color(0xFFB07219)
val LanguagePython = Color(0xFF3572A5)
val LanguageJavaScript = Color(0xFFF1E05A)
val LanguageTypeScript = Color(0xFF3178C6)
val LanguageSwift = Color(0xFFF05138)
val LanguageRust = Color(0xFFDEA584)
val LanguageGo = Color(0xFF00ADD8)
val LanguageCPlusPlus = Color(0xFFF34B7D)
val LanguageDefault = Color(0xFF004A77)

fun getLanguageColor(language: String?): Color {
    return when (language?.lowercase()) {
        "kotlin" -> LanguageKotlin
        "java" -> LanguageJava
        "python" -> LanguagePython
        "javascript", "js" -> LanguageJavaScript
        "typescript", "ts" -> LanguageTypeScript
        "swift" -> LanguageSwift
        "rust" -> LanguageRust
        "go" -> LanguageGo
        "c++", "cpp" -> LanguageCPlusPlus
        else -> LanguageDefault
    }
}
