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

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lairofpixies.choppity.Constants
import com.lairofpixies.choppity.logic.MainViewModel

@Composable
fun MainTopBar(viewModel: MainViewModel) {
    val callForInput = importCallbackFactory(viewModel)
    val callForOutput = exportCallbackFactory(viewModel)
    val hiresBitmap = viewModel.hiresBitmap.collectAsState()

    // for flipaction
    val normalColor = MaterialTheme.colorScheme.background
    val flippedColor = MaterialTheme.colorScheme.onBackground
    val currentColor = viewModel.appBackground.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.updateAppBackgroundColor(normalColor)
    }

    TopBarContent(
        outputAvailable = hiresBitmap.value != null,
        importAction = { callForInput() },
        exportAction = {
            hiresBitmap.value?.let { bitmap ->
                viewModel.launchExports(bitmap, callForOutput)
            }
        },
        rotateAction = { viewModel.increaseRotation() },
        flipAction = {
            viewModel.updateAppBackgroundColor(
                if (currentColor.value == normalColor) {
                    flippedColor
                } else {
                    normalColor
                }
            )
        }
    )
}


@Composable
fun importCallbackFactory(viewModel: MainViewModel): () -> Unit {
    var inputUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { inputUri = it }
        }

    LaunchedEffect(inputUri) {
        val uri = inputUri ?: return@LaunchedEffect
        viewModel.importImage(uri)
    }

    return { pickImageLauncher.launch(Constants.MIMETYPE_IMAGE) }
}

@Composable
fun exportCallbackFactory(viewModel: MainViewModel): (Bitmap, String) -> Unit {
    var outputUri by remember { mutableStateOf<Uri?>(null) }
    var partialBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val exportRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            Constants.MIMETYPE_IMAGE
        )
    ) { selectedUri: Uri? ->
        selectedUri?.let { outputUri = selectedUri }
    }

    LaunchedEffect(outputUri) {
        val exportUri = outputUri ?: return@LaunchedEffect
        val exportBitmap = partialBitmap ?: return@LaunchedEffect
        viewModel.exportSingle(exportBitmap, exportUri)
    }

    val callForOutput = { bitmap: Bitmap, suggestedFilename: String ->
        partialBitmap = bitmap
        exportRequest.launch(suggestedFilename)
    }
    return callForOutput
}