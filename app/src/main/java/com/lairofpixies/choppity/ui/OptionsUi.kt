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
 package com.lairofpixies.choppity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lairofpixies.choppity.Constants

enum class OptionCategories { ASPECT_RATIO, FILL_COLOR, SECTION_COUNT }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AspectRatioRow(
    setAspectRatio: (Size) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Constants.ASPECT_RATIOS.forEach { ar ->
            Button(
                onClick = {
                    setAspectRatio(Size(ar.first.toFloat(), ar.second.toFloat()))
                },
                modifier = Modifier.widthIn(60.dp)
            ) {
                if (ar.first > 0 && ar.second > 0) {
                    Text("${ar.first}:${ar.second}")
                } else {
                    Text("Orig")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorRow(
    setColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Constants.COLORS.forEach { color ->
            Button(
                onClick = {
                    setColor(color)
                }
            ) {
                Box(
                    Modifier
                        .size(36.dp, 24.dp)
                        .background(color)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionRow(
    setSections: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..10).forEach { count ->
            Button(
                onClick = {
                    setSections(count)
                },
                modifier = Modifier.widthIn(72.dp)
            ) {
                Text(text = count.toString())
            }
        }
    }
}
