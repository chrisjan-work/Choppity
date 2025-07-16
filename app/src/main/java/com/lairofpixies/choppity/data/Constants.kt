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
package com.lairofpixies.choppity.data

import androidx.compose.ui.graphics.Color

object Constants {
    // Generating export filenames
    const val DEFAULT_FILENAME = "image.JPG"
    const val EXPORT_PREFIX = "_edit"

    // Zoom controls
    const val RESET_ZOOM_ON_DOUBLE_TAP = true
    const val RESET_ZOOM_ON_RELEASE = false
    const val MINIMUM_ZOOM = 1f
    const val MAXIMUM_ZOOM = 20f

    // Mimetype
    const val MIMETYPE_IMAGE = "image/*"

    // Colors
    val FILL_COLOR_BLACK =  FillColor(Color(0xFF000000), "Black")
    val COLORS = listOf(
        FILL_COLOR_BLACK,
        FillColor(Color(0xFF0D0D0D), "05% Grey"),
        FillColor(Color(0xFF1A1A1A), "10% Grey"),
        FillColor(Color(0xFF333333), "20% Grey"),
        FillColor(Color(0xFF4D4D4D), "30% Grey"),
        FillColor(Color(0xFF808080), "50% Grey"),
        FillColor(Color(0xFFB3B3B3), "70% Grey"),
        FillColor(Color(0xFFCCCCCC), "80% Grey"),
        FillColor(Color(0xFFE6E6E6), "90% Grey"),
        FillColor(Color(0xFFFFFFFF), "White"),
    )

    enum class Rotations(val quarters: Int) {
        None(0),
        Quarter(1),
        Half(2),
        Threequts(3);

        fun increase(): Rotations = entries[(ordinal + 1) % entries.size]
    }
}