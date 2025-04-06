package com.lairofpixies.choppity

import androidx.compose.ui.graphics.Color

object Constants {
    // Zoom controls
    const val RESET_ZOOM_ON_DOUBLE_TAP = true
    const val RESET_ZOOM_ON_RELEASE = false
    const val MINIMUM_ZOOM = 1f
    const val MAXIMUM_ZOOM = 20f

    // Mimetype
    const val MIMETYPE_IMAGE = "image/*"

    // Known aspect ratios
    val ASPECT_RATIOS = listOf(
        0 to 0,
        3 to 4,
        5 to 6,
        1 to 1,
        6 to 5,
        4 to 3,
        7 to 5,
        3 to 2,
        16 to 9,
        20 to 9,
    )

    // Colors
    val COLORS = listOf(
        Color(0xFF000000), // Black
        Color(0xFF1A1A1A), // 10%
        Color(0xFF333333), // 20%
        Color(0xFF4D4D4D), // 30%
        Color(0xFF808080), // 50%
        Color(0xA6A6A6A6), // 65%
        Color(0xFFD9D9D9), // 85%
        Color(0xFFFFFFFF), // White
    )

    enum class Rotations(val quarters: Int) {
        none(0),
        quarter(1),
        half(2),
        threequts(3);

        fun increase(): Rotations = entries[(ordinal + 1) % entries.size]
    }
}