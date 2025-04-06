package com.lairofpixies.choppity.logic

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lairofpixies.choppity.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ProcessParams(
    val aspectRatio: Size,
    val bgColor: Color,
    val screenDimensions: Size,
    val turns: Constants.Rotations
)

class MainViewModel(
    private val context: Context
) : ViewModel() {

    private val _appBackground = MutableStateFlow(Color.Black)
    val appBackground = _appBackground.asStateFlow()

    private val _inputUri = MutableStateFlow<Uri?>(null)
    val inputUri = _inputUri.asStateFlow()

    private val _loadedBitmap = MutableStateFlow<Bitmap?>(null)
    val loadedBitmap = _loadedBitmap.asStateFlow()

    private val _hiresBitmap = MutableStateFlow<Bitmap?>(null)
    val hiresBitmap = _hiresBitmap.asStateFlow()

    private val _loresBitmap = MutableStateFlow<Bitmap?>(null)
    val loresBitmap = _loresBitmap.asStateFlow()

    private val _processParams = MutableStateFlow(
        ProcessParams(
            aspectRatio = Size(1f, 1f),
            bgColor = Color.Black,
            screenDimensions = Size(1920f, 1080f),
            turns = Constants.Rotations.none
        )
    )
    val processParams = _processParams.asStateFlow()

    init {
        viewModelScope.launch {
            listenToParameterChanges()
        }
    }

    fun importImage(uri: Uri?) {
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

                val params = ProcessParams(
                    aspectRatio = calculateDefaultAspectRatio(bitmap),
                    bgColor = processParams.value.bgColor,
                    screenDimensions = processParams.value.screenDimensions,
                    turns = Constants.Rotations.none
                )

                mutex.withLock {
                    _processParams.emit(params)
                    _loadedBitmap.emit(bitmap)
                }
            }
        }

        viewModelScope.launch {
            combine(
                loadedBitmap,
                processParams
            ) { bitmap, params ->
                if (bitmap == null) {
                    clearImageFlows()
                } else {
                    renderImageFlows(bitmap, params)
                }
            }.collect {}
        }
    }

    private suspend fun clearImageFlows() {
        _hiresBitmap.emit(null)
        _loresBitmap.emit(null)
    }

    private suspend fun renderImageFlows(
        inputBitmap: Bitmap,
        params: ProcessParams,
    ) {
        val processedBitmap = renderHires(inputBitmap, params)
        val downsizedBitmap = downsizeBitmap(processedBitmap, params.screenDimensions)

        mutex.withLock {
            _hiresBitmap.emit(processedBitmap)
            _loresBitmap.emit(downsizedBitmap)
        }
    }

    fun updateScreenSize(screenDimensionsInPx: Size) {
        viewModelScope.launch {
            _processParams.emit(processParams.value.copy(screenDimensions = screenDimensionsInPx))
        }
    }

    fun setAspectRatio(newAspectRatio: Size) {
        viewModelScope.launch {
            _processParams.emit(processParams.value.copy(aspectRatio = newAspectRatio))
        }
    }

    fun setColor(newColor: Color) {
        viewModelScope.launch {
            _processParams.emit(processParams.value.copy(bgColor = newColor))
        }
    }

    fun increaseRotation() {
        viewModelScope.launch {
            val newTurns = processParams.value.turns.increase()
            _processParams.emit(processParams.value.copy(turns = newTurns))
        }
    }

    fun updateAppColor(color: Color) {
        viewModelScope.launch {
            _appBackground.emit(color)
        }
    }

    fun export(bitmap: Bitmap, uri: Uri) {
        saveBitmapToUri(context, bitmap, uri)
    }
}