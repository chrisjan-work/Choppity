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
import android.net.Uri
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lairofpixies.choppity.data.AspectRatio
import com.lairofpixies.choppity.data.Constants
import com.lairofpixies.choppity.data.DialogStyle
import com.lairofpixies.choppity.data.FillColor
import com.lairofpixies.choppity.data.ProcessParams
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

    private val _busyIndicator = MutableStateFlow(DialogStyle.NONE)
    val busyIndicator = _busyIndicator.asStateFlow()

    private val _processParams = MutableStateFlow(ProcessParams.Default)

    val processParams = _processParams.asStateFlow()

    init {
        listenToParameterChanges()
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

                val params = processParams.value.copy(
                    aspectRatio = AspectRatio.Original,
                    turns = Constants.Rotations.None,
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

    fun setAspectRatio(newAspectRatio: AspectRatio) {
        viewModelScope.launch {
            val adjustedAspectRatio = if (newAspectRatio != AspectRatio.Auto) {
                newAspectRatio
            } else {
                loadedBitmap.value?.let { bitmap ->
                    calculateAutoAspectRatio(bitmap)
                } ?: AspectRatio.Original
            }
            _processParams.emit(processParams.value.copy(aspectRatio = adjustedAspectRatio))
        }
    }

    fun setRenderColor(newColor: FillColor) {
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

    private val exportQueue = mutableListOf<() -> Unit>()

    private fun consumeNextExport() {
        if (exportQueue.isNotEmpty()) {
            val nextCallback = exportQueue.removeAt(0)
            nextCallback.invoke()
        } else {
            toggleDialog(DialogStyle.NONE)
        }
    }

    private fun cancelExports() {
        exportQueue.clear()
        toggleDialog(DialogStyle.ERROR)
    }

    fun exportSingle(bitmap: Bitmap, uri: Uri) {
        if (diskLogic.saveBitmapToUri(bitmap, uri)) {
            consumeNextExport()
        } else {
            cancelExports()
        }
    }

    fun launchExports(bitmapToExport: Bitmap, callback: (Bitmap, String) -> Unit) {
        val originalUri = inputUri.value ?: return

        toggleDialog(DialogStyle.BUSY)
        val shouldStartQueue = exportQueue.isEmpty()
        populateQueue(originalUri, bitmapToExport, callback)

        if (shouldStartQueue) {
            consumeNextExport()
        } else {
            toggleDialog(DialogStyle.NONE)
        }
    }

    private fun populateQueue(originalUri: Uri, bitmapToExport: Bitmap, callback: (Bitmap, String) -> Unit) {
        val originalFilename = diskLogic.getFileNameFromUri(originalUri) ?: Constants.DEFAULT_FILENAME
        val basicOutputFilename = originalFilename.insertBeforeExtension(Constants.EXPORT_PREFIX)
        if (processParams.value.sectionCount <= 1) {
            exportQueue.add {
                callback(bitmapToExport, basicOutputFilename)
            }
        } else {
            val sections =
                choppify(bitmapToExport, processParams.value.sectionCount)
            for ((reverseIndex, sectionBitmap) in sections.reversed().withIndex()) {
                exportQueue.add {
                    val sectionFilename = basicOutputFilename.insertBeforeExtension("_$reverseIndex")
                    callback(sectionBitmap, sectionFilename)
                }
            }
        }
    }

    fun toggleDialog(dialogStyle: DialogStyle) {
        viewModelScope.launch {
            _busyIndicator.emit(dialogStyle)
        }
    }
}