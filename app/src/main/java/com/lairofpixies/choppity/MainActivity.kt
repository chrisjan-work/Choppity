package com.lairofpixies.choppity

//import androidx.compose.ui.graphics.graphicsLayer
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.lairofpixies.choppity.ui.theme.ChoppityTheme
import java.io.IOException


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChoppityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageOutputScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun DisplayPickedImage(imageUri: Uri?) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (imageUri != null) {
                ZoomableImage(imageUri = imageUri)
            } else {
                // Optionally display a placeholder or message when no image is selected
                // Example:
                // Text("No image selected", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }


    @Composable
    fun ImageExportScreen(imageUri: Uri?) {
        val context = LocalContext.current

        var originalFileName by remember { mutableStateOf<String?>(null) }
        var suggestedOutputUri by remember { mutableStateOf<Uri?>(null) }

        // Extract filename from Uri on composition if imageUri changes
        LaunchedEffect(imageUri) {
            if (imageUri != null) {
                originalFileName = getFileNameFromUri(context, imageUri)
                suggestedOutputUri = createSuggestedOutputUri(context, imageUri)
            } else {
                originalFileName = null
                suggestedOutputUri = null
            }
        }


        val exportImageLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIMETYPE_IMAGE)) { outputUri: Uri? ->
                if (outputUri != null && imageUri != null) {
                    saveImage(context, imageUri, outputUri)
                } else {
                    // Handle cancellation or error (e.g., show a message)
                }
            }

        Column {
            if (imageUri != null) {
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
            } else {
                Text("No image selected to export.")
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

    private fun saveImage(context: Context, inputUri: Uri, outputUri: Uri) {
        try {
            context.contentResolver.openInputStream(inputUri)?.use { input ->
                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            // Image saved successfully.  You might want to inform the user.
        } catch (e: IOException) {
            // Handle error (e.g., show a message)
        }
    }


    @Composable
    fun ImageOutputScreen(modifier: Modifier = Modifier) {
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

        val pickImageLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                selectedImageUri = uri
            }

        Column(modifier) {
            Button(onClick = { pickImageLauncher.launch(MIMETYPE_IMAGE) }) {
                Text("Pick Image")
            }
            //... your image display...

            ImageExportScreen(imageUri = selectedImageUri)

            DisplayPickedImage(imageUri = selectedImageUri)

        }

    }

    /* V3 */

    @Composable
    fun ZoomableImage(imageUri: Uri) {
        var scale by remember { mutableFloatStateOf(MINIMUM_ZOOM) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val transformableState =
            rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(MINIMUM_ZOOM, MAXIMUM_ZOOM)
                offset += panChange
            }

        if (RESET_ZOOM_ON_RELEASE) {
            LaunchedEffect(transformableState.isTransformInProgress) {
                if (!transformableState.isTransformInProgress) {
                    scale = 1.0f
                    offset = Offset.Zero
                }
            }
        }

        AsyncImage(
            model = imageUri,
            contentDescription = "Zoomable Image",
            contentScale = ContentScale.Fit, // Or other ContentScale options
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        if (RESET_ZOOM_ON_DOUBLETAP) {
                            scale = MINIMUM_ZOOM
                            offset = Offset.Zero
                        }
                    })
                }
                .transformable(transformableState)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }

    companion object {
        const val MIMETYPE_IMAGE = "image/*"
        const val RESET_ZOOM_ON_DOUBLETAP = true
        const val RESET_ZOOM_ON_RELEASE = false
        private const val MINIMUM_ZOOM = 1f
        private const val MAXIMUM_ZOOM = 20f
    }
}
