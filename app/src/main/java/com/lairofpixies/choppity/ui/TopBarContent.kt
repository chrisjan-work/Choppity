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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun TopBarContent(
    modifier: Modifier = Modifier,
    outputAvailable: Boolean,
    importAction: () -> Unit,
    exportAction: () -> Unit,
    rotateAction: () -> Unit,
    flipAction: () -> Unit
) {
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .statusBarsPadding(),
        content = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                FlipButton(flipAction)
                RotateIcon(outputAvailable, rotateAction)

                Spacer(Modifier.weight(1f))

                ImportIcon(importAction)
                ExportIcon(outputAvailable, exportAction)
            }
        }
    )
}

@Composable
fun ImportIcon(importAction: () -> Unit) {
    Icon(
        Icons.Default.Add,
        contentDescription = "Import",
        modifier =
            Modifier
                .padding(8.dp)
                .clickable { importAction() },
        tint = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun ExportIcon(
    outputAvailable: Boolean,
    exportAction: () -> Unit
) {
    val exportModifier = if (outputAvailable) {
        Modifier
            .padding(8.dp)
            .clickable { exportAction() }
    } else {
        Modifier
            .padding(8.dp)
            .alpha(0.2f)
    }

    Icon(
        Icons.Default.Done,
        contentDescription = "Export",
        modifier = exportModifier,
        tint = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun RotateIcon(
    outputAvailable: Boolean,
    rotateAction: () -> Unit
) {
    val rotateModifier = if (outputAvailable) {
        Modifier
            .padding(8.dp)
            .clickable { rotateAction() }
    } else {
        Modifier
            .padding(8.dp)
            .alpha(0.2f)
    }

    Icon(
        Icons.Default.Refresh,
        contentDescription = "Rotate",
        modifier = rotateModifier,
        tint = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun FlipButton(
    flipAction: () -> Unit
) {
    Icon(
        Icons.Default.Person,
        contentDescription = "Flip",
        modifier =
            Modifier
                .padding(8.dp)
                .clickable { flipAction() },
        tint = MaterialTheme.colorScheme.onBackground,
    )
}
