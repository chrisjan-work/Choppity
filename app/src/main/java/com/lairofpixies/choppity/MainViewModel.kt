package com.lairofpixies.choppity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class MainViewModel(
    private val context: Context
) : ViewModel() {

    private val _inputUri = MutableStateFlow<Uri?>(null)
    val inputUri = _inputUri.asStateFlow()

    private val _outputBitmap = MutableStateFlow<Bitmap?>(null)
    val outputBitmap = _outputBitmap.asStateFlow()

    private val _loresBitmap = MutableStateFlow<Bitmap?>(null)
    val loresBitmap = _loresBitmap.asStateFlow()

    private val _aspectRatio = MutableStateFlow(Size(1f, 1f))
    val aspectRatio = _aspectRatio.asStateFlow()

    // TODO: update based on actual values
    private val _screenDimensions = MutableStateFlow(Size(1920f, 1080f))
    val screenDimensions = _screenDimensions.asStateFlow()

    fun loadImage(uri: Uri?) {
        viewModelScope.launch {
            _inputUri.emit(uri)
            if (uri == null) return@launch

            val loadedBitmap = loadBitmapFromUri(context, uri)
            if (loadedBitmap == null) {
                _outputBitmap.emit(null)
                return@launch
            }

            val desiredDimensions = calculateDimensions(loadedBitmap, aspectRatio.value)
            val processedBitmap = createResizedBitmap(loadedBitmap, desiredDimensions, Color.Black)
            _outputBitmap.emit(processedBitmap)

            val downsizedBitmap = downsizeBitmap(processedBitmap, screenDimensions.value)
            _loresBitmap.emit(downsizedBitmap)
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateDimensions(bitmap: Bitmap, desiredAspectRatio: Size): Size {
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

    private fun createResizedBitmap(
        originalBitmap: Bitmap,
        targetDimensions: Size,
        backgroundColor: Color
    ): Bitmap {
        val targetWidth = targetDimensions.width.toInt()
        val targetHeight = targetDimensions.height.toInt()
        require(targetWidth > 0 && targetHeight > 0)

        val resizedBitmap = createBitmap(targetWidth, targetHeight)
//        val resizedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(resizedBitmap)

        // Calculate offsets to center the original image
        val offsetX = (targetWidth - originalBitmap.width) / 2f
        val offsetY = (targetHeight - originalBitmap.height) / 2f

        // Draw the original bitmap centered in the new canvas
        canvas.drawColor(backgroundColor.toArgb())
        canvas.drawBitmap(originalBitmap, offsetX, offsetY, null)

        return resizedBitmap
    }

    private fun downsizeBitmap(hiresBitmap: Bitmap, screenDimensions: Size): Bitmap {
        require(hiresBitmap.width > 0 && hiresBitmap.height > 0)
        require(screenDimensions.width > 0 && screenDimensions.height > 0)
        val scaleFactor: Float = min(
            screenDimensions.width / hiresBitmap.width,
            screenDimensions.height / hiresBitmap.height
        )

        val resizedBitmap =
            createBitmap(screenDimensions.width.toInt(), screenDimensions.height.toInt())
        val canvas = android.graphics.Canvas(resizedBitmap)
        val matrix = Matrix().apply {
            setScale(scaleFactor, scaleFactor) // Scale down to 10% of original size
        }

        canvas.drawBitmap(hiresBitmap, matrix, null)

        return resizedBitmap
    }

    fun updateScreenSize(screenDimensionsInPx: Size) {
        viewModelScope.launch {
            _screenDimensions.emit(screenDimensionsInPx)
        }
    }
}