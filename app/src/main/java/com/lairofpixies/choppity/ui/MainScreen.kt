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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.lairofpixies.choppity.data.DialogStyle
import com.lairofpixies.choppity.logic.MainViewModel


@Composable
fun MainScreen(viewModel: MainViewModel) {
    MaterialTheme.colorScheme.background.let { initialBackground ->
        LaunchedEffect(initialBackground) {
            viewModel.updateAppBackgroundColor(initialBackground)
        }
    }

    MainScreenScaffold(
        viewModel,
        topBar = { MainTopBar(viewModel) },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        ScreenDimensionsUpdater { screenSize -> viewModel.updateScreenSize(screenSize) }
        CentralView(
            viewModel,
            modifier = Modifier
                .padding(innerPadding)
                .background(viewModel.appBackground.collectAsState().value)
        )
    }
}

@Composable
fun CentralView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        // image
        val loresBitmap = viewModel.loresBitmap.collectAsState()
        ProcessedImageDisplay(loresBitmap.value, modifier = Modifier.weight(1f))
    }

    val showBusy = viewModel.busyIndicator.collectAsState().value
    val dismissDialog = { viewModel.toggleDialog(DialogStyle.NONE) }
    when (showBusy) {
        DialogStyle.BUSY ->
            ProgressDialog(dismissDialog)
        DialogStyle.ERROR ->
            ErrorDialog(dismissDialog)
        else -> {}
    }
}