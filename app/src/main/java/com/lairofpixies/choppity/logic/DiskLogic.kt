package com.lairofpixies.choppity.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

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