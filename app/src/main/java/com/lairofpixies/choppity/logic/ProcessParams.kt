package com.lairofpixies.choppity.logic

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.lairofpixies.choppity.Constants

data class ProcessParams(
    val aspectRatio: Size,
    val bgColor: Color,
    val screenDimensions: Size,
    val turns: Constants.Rotations
)