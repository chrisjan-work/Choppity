package com.lairofpixies.choppity

import androidx.compose.ui.graphics.Color

object Constants {
    // Zoom controls
    const val RESET_ZOOM_ON_DOUBLETAP = true
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
        Color(0xFF0D0D0D),// 5%
        Color(0xFF1A1A1A),// 10%
        Color(0xFF333333),// 20%
        Color(0xFF4D4D4D),// 30%
        Color(0xFF808080),// 50%
        Color(0xFFCCCCCC),// 80%
        Color(0xFFFFFFFF), // White
    )
}