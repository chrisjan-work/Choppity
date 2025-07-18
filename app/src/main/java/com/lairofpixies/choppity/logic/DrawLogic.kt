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
package com.lairofpixies.choppity.logic

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.lairofpixies.choppity.data.Constants
import com.lairofpixies.choppity.data.AspectRatio
import com.lairofpixies.choppity.data.ProcessParams
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

internal fun calculateAutoAspectRatio(bitmap: Bitmap): AspectRatio {
    return AspectRatio.All
        .filterIsInstance<AspectRatio.Value>()
        .minByOrNull {
            val newDimensions = calculateDimensions(bitmap, it)
            val dx = bitmap.width - newDimensions.width
            val dy = bitmap.height - newDimensions.height
            (dx * dx) + (dy * dy) // quadratic error
        } ?: AspectRatio.Square
}

internal fun calculateDimensions(bitmap: Bitmap, desiredAspectRatio: AspectRatio.Value): Size {
    val originalWidth = bitmap.width.toFloat()
    val originalHeight = bitmap.height.toFloat()
    require(originalWidth > 0 && originalHeight > 0)

    val expandedWidth =
        floor(
            max(
                originalWidth,
                originalHeight * desiredAspectRatio.width / desiredAspectRatio.height
            )
        )
    val expandedHeight =
        floor(
            max(
                originalHeight,
                originalWidth * desiredAspectRatio.height / desiredAspectRatio.width
            )
        )

    return Size(expandedWidth, expandedHeight)
}

internal fun rotateBitmapQuarterTurns(input: Bitmap, turns: Int): Bitmap {
    require(turns in 0..3)

    val degrees = 90 * turns
    val matrix = Matrix().apply {
        postRotate(degrees.toFloat())
    }

    return Bitmap.createBitmap(
        input,
        0, 0,
        input.width, input.height,
        matrix,
        true
    )
}

internal fun createResizedBitmap(
    originalBitmap: Bitmap,
    targetDimensions: Size,
    backgroundColor: Color,
): Bitmap {
    val targetWidth = targetDimensions.width.toInt()
    val targetHeight = targetDimensions.height.toInt()
    require(targetWidth > 0 && targetHeight > 0)

    val resizedBitmap = createBitmap(targetWidth, targetHeight)
    val canvas = android.graphics.Canvas(resizedBitmap)

    // Calculate offsets to center the original image
    val offsetX = (targetWidth - originalBitmap.width) / 2f
    val offsetY = (targetHeight - originalBitmap.height) / 2f

    // Draw the original bitmap centered in the new canvas
    canvas.drawColor(backgroundColor.toArgb())
    canvas.drawBitmap(originalBitmap, offsetX, offsetY, null)

    return resizedBitmap
}

internal fun renderHires(inputBitmap: Bitmap, params: ProcessParams): Bitmap {
    val rotatedBitmap = if (params.turns != Constants.Rotations.None) {
        rotateBitmapQuarterTurns(inputBitmap, params.turns.quarters)
    } else {
        inputBitmap
    }

    val processedBitmap =
        if (params.aspectRatio !is AspectRatio.Value) {
            // special case: skip resizing
            rotatedBitmap
        } else {
            val desiredDimensions = calculateDimensions(inputBitmap, params.aspectRatio)
            createResizedBitmap(rotatedBitmap, desiredDimensions, params.bgColor.color)
        }

    return processedBitmap
}

internal fun downsizeBitmap(hiresBitmap: Bitmap, params: ProcessParams, lineColor: Color): Bitmap {
    require(hiresBitmap.width > 0 && hiresBitmap.height > 0)
    require(params.screenDimensions.width > 0 && params.screenDimensions.height > 0)
    val scaleFactor: Float = min(
        params.screenDimensions.width / hiresBitmap.width,
        params.screenDimensions.height / hiresBitmap.height
    )
    val outputDimensions = Size(
        hiresBitmap.width * scaleFactor,
        hiresBitmap.height * scaleFactor
    )

    val resizedBitmap =
        createBitmap(outputDimensions.width.toInt(), outputDimensions.height.toInt())
    val canvas = android.graphics.Canvas(resizedBitmap)
    val matrix = Matrix().apply {
        setScale(scaleFactor, scaleFactor) // Scale down to 10% of original size
    }

    // draw the bitmap
    canvas.drawBitmap(hiresBitmap, matrix, null)
    // draw the separators in the output
    if (params.sectionCount > 1) {
        val paint = Paint().apply {
            color = lineColor.toArgb()  // Set line color
            strokeWidth = 1f     // Line thickness
            isAntiAlias = false   // Smooth edges
        }
        for (i in 1 until params.sectionCount) {
            val x = outputDimensions.width * i / params.sectionCount
            canvas.drawLine(x, 0f, x, outputDimensions.height, paint)
        }
    }

    return resizedBitmap
}

internal fun choppify(hiresBitmap: Bitmap, sectionCount: Int): List<Bitmap> {
    val side = floor(hiresBitmap.width.toFloat() / sectionCount).toInt()
    val margin = hiresBitmap.width - sectionCount * side
    val marginStart = margin / 2
    val marginEnd = margin - marginStart

    val sectionList = mutableListOf<Bitmap>()
    for (i in 0 until sectionCount) {
        val sectionBitmap = createBitmap(side, hiresBitmap.height)
        val canvas = android.graphics.Canvas(sectionBitmap)
        val x = marginStart + i * side
        canvas.drawBitmap(
            hiresBitmap,
            Rect(x, 0, x + side, hiresBitmap.height),
            Rect(0, 0, side, hiresBitmap.height),
            null
        )
        sectionList.add(sectionBitmap)
    }

    return sectionList
}