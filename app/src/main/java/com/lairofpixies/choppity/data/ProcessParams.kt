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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

data class ProcessParams(
    val aspectRatio: AspectRatio,
    val bgColor: FillColor,
    val screenDimensions: Size,
    val turns: Constants.Rotations,
    val sectionCount: Int
) {
    companion object {
        val Default = ProcessParams(
            aspectRatio = AspectRatio.Original,
            bgColor = Constants.FILL_COLOR_BLACK,
            screenDimensions = Size(1920f, 1080f),
            turns = Constants.Rotations.None,
            sectionCount = 1
        )
    }
}
