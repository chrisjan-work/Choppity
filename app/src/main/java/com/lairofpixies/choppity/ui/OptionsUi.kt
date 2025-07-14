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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lairofpixies.choppity.data.AspectRatio
import com.lairofpixies.choppity.data.Constants


enum class OptionCategories { ASPECT_RATIO, FILL_COLOR, SECTION_COUNT }

@Composable
fun AspectRatioRow(
    modifier: Modifier = Modifier,
    setAspectRatio: (AspectRatio) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(AspectRatio.All) { item ->
            AspectRatioButton(item, onClick = { setAspectRatio(item) })
        }
    }
}

@Composable
fun AspectRatioButton(item: AspectRatio, onClick: () -> Unit) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .aspectRatio(1.5f)
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = item.label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview
@Composable
fun AspectRatioRowPreview() {
    AspectRatioRow {}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorRow(
    modifier: Modifier = Modifier,
    setColor: (Color) -> Unit,
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

@Preview
@Composable
fun ColorRowPreview() {
    ColorRow {}
}

@Composable
fun SectionRow(
    modifier: Modifier = Modifier,
    setSections: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items((1..10).toList()) { sectionCount ->
            SectionButton(sectionCount, onClick = { setSections(sectionCount) })
        }
    }
}

@Composable
fun SectionButton(sectionCount: Int, onClick: () -> Unit) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .aspectRatio(1.5f)
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = sectionCount.toString(), style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Preview
@Composable
fun SectionRowPreview() {
    SectionRow {}
}