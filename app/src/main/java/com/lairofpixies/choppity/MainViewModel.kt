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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class MainViewModel(
    private val context: Context
) : ViewModel() {

    private val _inputUri = MutableStateFlow<Uri?>(null)
    val inputUri = _inputUri.asStateFlow()

    private val _loadedBitmap = MutableStateFlow<Bitmap?>(null)
    val loadedBitmap = _loadedBitmap.asStateFlow()

    private val _hiresBitmap = MutableStateFlow<Bitmap?>(null)
    val hiresBitmap = _hiresBitmap.asStateFlow()

    private val _loresBitmap = MutableStateFlow<Bitmap?>(null)
    val loresBitmap = _loresBitmap.asStateFlow()

    private val _aspectRatio = MutableStateFlow(Size(1f, 1f))
    val aspectRatio = _aspectRatio.asStateFlow()

    // TODO: update based on actual values
    private val _screenDimensions = MutableStateFlow(Size(1920f, 1080f))
    val screenDimensions = _screenDimensions.asStateFlow()

    init {
        viewModelScope.launch {
            listenToParameterChanges()
        }
    }

    fun loadImage(uri: Uri?) {
        viewModelScope.launch {
            _inputUri.emit(uri)
        }
    }

    private val mutex = Mutex()
    private fun listenToParameterChanges() {
        viewModelScope.launch {
            inputUri.collect { uri ->
                val bitmap = uri?.let { loadBitmapFromUri(context, uri) }
                if (bitmap == null) {
                    _loadedBitmap.emit(null)
                    return@collect
                }

                val ratio = calculateDefaultAspectRatio(bitmap)
                mutex.withLock {
                    _loadedBitmap.emit(bitmap)
                    _aspectRatio.emit(ratio)
                }
            }
        }
        viewModelScope.launch {
            combine(loadedBitmap, aspectRatio, screenDimensions) { bitmap, ratio, dimensions ->
                if (bitmap == null) {
                    clearImage()
                } else {
                    renderImage(bitmap, ratio, dimensions)
                }
            }.collect {}
        }
    }

    private fun calculateDefaultAspectRatio(bitmap: Bitmap): Size {
        return ASPECT_RATIOS.map {
            Size(it.first.toFloat(), it.second.toFloat())
        }.minByOrNull {
            val newDimensions = calculateDimensions(bitmap, it)
            val dx = bitmap.width - newDimensions.width
            val dy = bitmap.height - newDimensions.height
            (dx * dx) + (dy * dy) // quadratic error
        } ?: Size(1f, 1f)
    }

    private suspend fun clearImage() {
        _hiresBitmap.emit(null)
        _loresBitmap.emit(null)
    }

    private suspend fun renderImage(inputBitmap: Bitmap, ratio: Size, dimensions: Size) {
        val desiredDimensions = calculateDimensions(inputBitmap, ratio)
        val processedBitmap = createResizedBitmap(inputBitmap, desiredDimensions, Color.Black)
        _hiresBitmap.emit(processedBitmap)

        val downsizedBitmap = downsizeBitmap(processedBitmap, dimensions)
        _loresBitmap.emit(downsizedBitmap)
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

        canvas.drawBitmap(hiresBitmap, matrix, null)

        return resizedBitmap
    }

    fun updateScreenSize(screenDimensionsInPx: Size) {
        viewModelScope.launch {
            _screenDimensions.emit(screenDimensionsInPx)
        }
    }

    fun setAspectRatio(newAspectRatio: Size) {
        viewModelScope.launch {
            _aspectRatio.emit(newAspectRatio)
        }
    }

    fun saveBitmapToUri(bitmap: Bitmap, uri: Uri) {
        try {
            // TODO: display error
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val ASPECT_RATIOS = listOf(
            3 to 4,
            5 to 6,
            1 to 1,
            6 to 5,
            4 to 3,
            7 to 5,
            3 to 2,
            16 to 9,
            20 to 9,
        )
    }
}