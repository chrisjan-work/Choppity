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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lairofpixies.choppity.R
import com.lairofpixies.choppity.data.Constants


@Composable
fun ScreenDimensionsUpdater(onDimensionsAvailable: (Size) -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) {
        configuration.screenWidthDp.dp.roundToPx()
    }
    val screenHeightPx = with(density) {
        configuration.screenHeightDp.dp.roundToPx()
    }

    val screenSize = Size(screenWidthPx.toFloat(), screenHeightPx.toFloat())
    LaunchedEffect(Unit) {
        onDimensionsAvailable(screenSize)
    }
}

@Composable
fun ProcessedImageDisplay(loresBitmap: Bitmap?, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (loresBitmap != null) {
            ZoomableBitmap(loresBitmap)
        } else {
            Text(text = stringResource(R.string.no_image_loaded))
        }
    }
}


@Composable
fun ZoomableBitmap(bitmap: Bitmap, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(Constants.MINIMUM_ZOOM) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            scale =
                (scale * zoomChange).coerceIn(Constants.MINIMUM_ZOOM, Constants.MAXIMUM_ZOOM)
            offset += panChange
        }

    if (Constants.RESET_ZOOM_ON_RELEASE) {
        LaunchedEffect(transformableState.isTransformInProgress) {
            if (!transformableState.isTransformInProgress) {
                scale = 1.0f
                offset = Offset.Zero
            }
        }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .bitmapConfig(Bitmap.Config.RGB_565) // Reduce memory usage
            .allowHardware(false) // Force software rendering if needed
            .build()
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(bitmap)
            .size(bitmap.width, bitmap.height) // Original dimensions
            .build(),
        contentDescription = stringResource(R.string.CD_zoomable_image),
        imageLoader = imageLoader,
        contentScale = ContentScale.Fit, // Or other ContentScale options
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (Constants.RESET_ZOOM_ON_DOUBLE_TAP) {
                        scale = Constants.MINIMUM_ZOOM
                        offset = Offset.Zero
                    }
                })
            }
            .transformable(transformableState)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    )
}