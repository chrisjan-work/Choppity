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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore

class DiskLogic(private val context: Context) {
    internal fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    internal fun saveBitmapToUri(bitmap: Bitmap, uri: Uri) {
        try {
            // TODO: display error
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper function to get the file name from a content URI (if possible)
    fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameColumnIndex =
                        it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (displayNameColumnIndex != -1) {
                        fileName = it.getString(displayNameColumnIndex)
                    }
                }
            }
        } else if (uri.scheme == "file") {
            fileName = uri.lastPathSegment
        }
        return fileName
    }
}

fun String.insertBeforeExtension(insertText: String): String {
    val base = substringBeforeLast(".")
    val extension = substringAfterLast(".", "")
    return if (extension.isNotEmpty()) "$base$insertText.$extension" else this
}