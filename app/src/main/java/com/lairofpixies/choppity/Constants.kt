/*
 * This file is part of Choppity.
 *
 * Copyright (C) 2025 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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