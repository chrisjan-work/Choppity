package com.lairofpixies.choppity.logic

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

class MainViewModel(
    private val diskLogic: DiskLogic
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

    private val _busyIndicator = MutableStateFlow(false)
    val busyIndicator = _busyIndicator.asStateFlow()

    private val _processParams = MutableStateFlow(
        ProcessParams(
            aspectRatio = Size(1f, 1f),
            bgColor = Color.Black,
            screenDimensions = Size(1920f, 1080f),
            turns = Constants.Rotations.none,
            sectionCount = 1
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
                val bitmap = uri?.let { diskLogic.loadBitmapFromUri(uri) }
                if (bitmap == null) {
                    _loadedBitmap.emit(null)
                    return@collect
                }

                val params = ProcessParams(
                    aspectRatio = calculateDefaultAspectRatio(bitmap),
                    bgColor = processParams.value.bgColor,
                    screenDimensions = processParams.value.screenDimensions,
                    turns = Constants.Rotations.none,
                    sectionCount = 1
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
                processParams,
                appBackground
            ) { bitmap, params, lineColor ->
                if (bitmap == null) {
                    clearImageFlows()
                } else {
                    renderImageFlows(bitmap, params, lineColor)
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
        lineColor: Color,
    ) {
        val processedBitmap = renderHires(inputBitmap, params)
        val downsizedBitmap = downsizeBitmap(processedBitmap, params, lineColor)

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

    fun setRenderColor(newColor: Color) {
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

    fun updateAppBackgroundColor(color: Color) {
        viewModelScope.launch {
            _appBackground.emit(color)
        }
    }

    fun setSections(sectionCount: Int) {
        viewModelScope.launch {
            _processParams.emit(processParams.value.copy(sectionCount = sectionCount))
        }
    }

//    fun _export(bitmap: Bitmap, uri: Uri) {
//        if (processParams.value.sectionCount <= 1) {
//            diskLogic.saveBitmapToUri(bitmap, uri)
//        } else {
//            TODO("doesn't quite work yet. The chopping part works, but writing files is not so easy")
//            // chop in parts, save the parts
//            viewModelScope.launch {
//                val sections =
//                    choppify(bitmap, processParams.value.sectionCount)
//                val originalFilename = diskLogic.getFileNameFromUri(uri)
//                for (section in sections.withIndex()) {
//                    val sectionUri =
//                        uri.toString()?.insertBeforeExtension("_${section.index}")?.toUri()
//                    sectionUri?.let {
//                        diskLogic.saveBitmapToUri(section.value, sectionUri)
//                    }
//                }
//            }
//        }
//    }

    private val exportQueue = mutableListOf<() -> Unit>()

    private fun consumeNextExport() {
        if (exportQueue.isNotEmpty()) {
            val nextCallback = exportQueue.removeAt(0)
            nextCallback.invoke()
        } else {
            toggleBusy(false)
        }
    }

    fun exportSingle(bitmap: Bitmap, uri: Uri) {
        diskLogic.saveBitmapToUri(bitmap, uri)
        consumeNextExport()
    }

    fun launchExports(hiresBitmap: Bitmap, callback: (Bitmap, String) -> Unit) {
        toggleBusy(true)
        val shouldStartQueue = exportQueue.isEmpty()
        val originalUri = inputUri.value ?: return
        val originalFilename = diskLogic.getFileNameFromUri(originalUri) ?: "image.JPG"
        val basicOutputFilename = originalFilename.insertBeforeExtension("_edit")
        if (processParams.value.sectionCount <= 1) {
            exportQueue.add {
                callback(hiresBitmap, basicOutputFilename)
            }
        } else {
            val sections =
                choppify(hiresBitmap, processParams.value.sectionCount)
            for ((index, sectionBitmap) in sections.withIndex()) {
                exportQueue.add {
                    val sectionFilename = basicOutputFilename.insertBeforeExtension("_$index")
                    callback(sectionBitmap, sectionFilename)
                }
            }
        }

        if (shouldStartQueue) {
            consumeNextExport()
        }
    }

    fun toggleBusy(busy: Boolean) {
        viewModelScope.launch {
            _busyIndicator.emit(busy)
        }
    }
}