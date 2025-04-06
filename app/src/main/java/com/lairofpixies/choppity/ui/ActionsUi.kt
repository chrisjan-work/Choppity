package com.lairofpixies.choppity.ui

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.lairofpixies.choppity.Constants


@Composable
fun ActionRow(
    inputUri: Uri?,
    outputAvailable: Boolean,
    importAction: (Uri) -> Unit,
    exportAction: (Uri) -> Unit,
    rotateAction: () -> Unit,
) {
    Row {
        ImportButton { newUri ->
            importAction(newUri)
        }

        if (inputUri != null && outputAvailable) {
            ExportButton(
                contentUri = inputUri
            ) { outputUri ->
                exportAction(outputUri)
            }

            RotateButton(rotateAction)
        }
    }
}

@Composable
fun ImportButton(uriSelected: (uri: Uri) -> Unit) {
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uriSelected(it) }
        }

    Button(onClick = { pickImageLauncher.launch(Constants.MIMETYPE_IMAGE) }) {
        Text("Pick Image")
    }
}

@Composable
fun ExportButton(contentUri: Uri?, onClick: (Uri) -> Unit) {
    val context = LocalContext.current

    var originalFileName by remember { mutableStateOf<String?>(null) }
    var suggestedOutputUri by remember { mutableStateOf<Uri?>(null) }


    // Extract filename from Uri on composition if imageUri changes
    LaunchedEffect(contentUri) {
        if (contentUri != null) {
            originalFileName = getFileNameFromUri(context, contentUri)
            suggestedOutputUri = createSuggestedOutputUri(context, contentUri)
        } else {
            originalFileName = null
            suggestedOutputUri = null
        }
    }

    if (contentUri != null) {
        val exportImageLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument(
                    Constants.MIMETYPE_IMAGE
                )
            ) { outputUri: Uri? ->
                // TODO: handle cancellations
                outputUri?.let { onClick(it) }
            }

        Button(onClick = {
            // Launch the file picker with a suggested filename and location
            suggestedOutputUri?.let {
                val suggestedFilename =
                    originalFileName?.replaceAfterLast('.', "JPG")?.replace(".", "_ed.")
                exportImageLauncher.launch(suggestedFilename)
            }
        }) {
            Text("Export")
        }
    }
}

// Helper function to get the file name from a content URI (if possible)
private fun getFileNameFromUri(context: Context, uri: Uri): String? {
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

// Helper function to create a suggested output URI
private fun createSuggestedOutputUri(context: Context, imageUri: Uri): Uri? {
    val fileName = getFileNameFromUri(context, imageUri) ?: "exported_image.jpg"
    val baseName = fileName.substringBeforeLast('.')
    val extension = fileName.substringAfterLast('.', "jpg") // Default to jpg if no extension
    val newFileName = "${baseName}_ed.$extension"

    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/$extension") // Or appropriate MIME type
        put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES
        ) // Or other directory
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}

@Composable
fun RotateButton(rotateAction: () -> Unit) {
    Button(onClick = rotateAction) {
        Text("Rotate")
    }
}