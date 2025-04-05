package com.lairofpixies.choppity

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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.lairofpixies.choppity.ui.theme.ChoppityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.max


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

//            DisplayPickedImage(imageUri = selectedImageUri)
            BorderedImageScreen(selectedImageUri)

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

    /* v4 */
    @Composable
    fun BorderedImage(
        imageUri: Uri,
        widthFactor: Float,
        heightFactor: Float,
        modifier: Modifier = Modifier,
        backgroundColor: Color = Color.Black,
    ) {
        val context = LocalContext.current
        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var canvasSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(imageUri, widthFactor, heightFactor) {
            imageBitmap = loadImageBitmap(context, imageUri)
        }

        // TODO: this is scaling DOWN the image to fit the canvas
        // we want to scale UP the canvas, and then zoom out to fit
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(widthFactor / heightFactor)
                .onSizeChanged { canvasSize = it }
        ) {
            if (imageBitmap != null) {
                val originalWidth = imageBitmap!!.width.toFloat()
                val originalHeight = imageBitmap!!.height.toFloat()

                val expandedCanvasWidth = max(originalWidth, originalHeight * widthFactor / heightFactor)
                val expandedCanvasHeight = max(originalHeight, originalWidth * heightFactor / widthFactor)

                val drawWidth = originalWidth * canvasSize.width / expandedCanvasWidth
                val drawHeight = originalHeight * canvasSize.height / expandedCanvasHeight

                val offsetX = (canvasSize.width - drawWidth) / 2f
                val offsetY = (canvasSize.height - drawHeight) / 2f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = backgroundColor) // Draw background
                    translate(left = offsetX, top = offsetY) {
                        drawImage(
                            image = imageBitmap!!,
                            dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt())
                        )
                    }
                }
            } else {
                // Placeholder while loading
                Text("Loading...", modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    private suspend fun loadImageBitmap(context: android.content.Context, uri: Uri): ImageBitmap? =
        withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(uri)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                result.drawable.toBitmap().asImageBitmap()
            } else {
                null
            }
        }
    @Composable
    fun BorderedImageScreen(imageUri: Uri?) {
        var widthFactor by remember { mutableFloatStateOf(16f) }
        var heightFactor by remember { mutableFloatStateOf(9f) }

        Column(Modifier.fillMaxSize()) {
            if (imageUri != null) {
                BorderedImage(
                    imageUri = imageUri,
                    widthFactor = widthFactor,
                    heightFactor = heightFactor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                // Optional controls to change widthFactor and heightFactor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { widthFactor = 1f; heightFactor = 1f }) {
                        Text("1:1")
                    }
                    Button(onClick = { widthFactor = 4f; heightFactor = 3f }) {
                        Text("4:3")
                    }
                    Button(onClick = { widthFactor = 3f; heightFactor = 2f }) {
                        Text("3:2")
                    }
                    Button(onClick = { widthFactor = 16f; heightFactor = 9f }) {
                        Text("16:9")
                    }
                }
            } else {
                Text("No Image Selected", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }

    companion object {
        const val MIMETYPE_IMAGE = "image/*"
        const val RESET_ZOOM_ON_DOUBLETAP = true
        const val RESET_ZOOM_ON_RELEASE = false
        private const val MINIMUM_ZOOM = 1f
        private const val MAXIMUM_ZOOM = 20f
    }
}
