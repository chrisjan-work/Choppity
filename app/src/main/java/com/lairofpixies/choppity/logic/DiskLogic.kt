package com.lairofpixies.choppity.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore

internal fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

internal fun saveBitmapToUri(context: Context, bitmap: Bitmap, uri: Uri) {
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
fun getFileNameFromUri(context: Context, uri: Uri): String? {
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

fun String.insertBeforeExtension(insertText: String): String {
    val base = substringBeforeLast(".")
    val extension = substringAfterLast(".", "")
    return if (extension.isNotEmpty()) "$base$insertText.$extension" else this
}