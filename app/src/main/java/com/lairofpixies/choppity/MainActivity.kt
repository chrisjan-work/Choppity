package com.lairofpixies.choppity

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lairofpixies.choppity.ui.theme.ChoppityTheme


class MainActivity : ComponentActivity() {

    val viewModel = MainViewModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChoppityTheme {
                ScreenDimensionsUpdater()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun ScreenDimensionsUpdater() {
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current

        val screenWidthPx = with(density) {
            configuration.screenWidthDp.dp.roundToPx()
        }
        val screenHeightPx = with(density) {
            configuration.screenHeightDp.dp.roundToPx()
        }

        val screenSize = Size(screenWidthPx.toFloat(), screenHeightPx.toFloat())
        LaunchedEffect(Unit) {
            viewModel.updateScreenSize(screenSize)
        }
    }

    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {
        Column(modifier.fillMaxSize()) {
            // Input
            ActionRow(viewModel)
            // image
            ProcessedImageDisplay(viewModel, modifier = Modifier.weight(1f))
            // aspect ratio
            OptionsRow(viewModel)
        }
    }

    @Composable
    fun ActionRow(viewModel: MainViewModel) {
        Row {
            LoadButton(viewModel)
            ExportButton(viewModel)
        }
    }

    @Composable
    fun OptionsRow(viewModel: MainViewModel) {
        Column {
            AspectRatioRow(viewModel)
        }
    }

    @Composable
    fun LoadButton(viewModel: MainViewModel) {
        val pickImageLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { viewModel.loadImage(it) }
            }

        Button(onClick = { pickImageLauncher.launch(MIMETYPE_IMAGE) }) {
            Text("Pick Image")
        }
    }

    @Composable
    fun ProcessedImageDisplay(viewModel: MainViewModel, modifier: Modifier = Modifier) {
        val bitmap = viewModel.loresBitmap.collectAsState().value

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                ZoomableBitmap(bitmap)
            } else {
                Text(text = "No image loaded")
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun AspectRatioRow(viewModel: MainViewModel, modifier: Modifier = Modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MainViewModel.ASPECT_RATIOS.forEach { ar ->
                Button(onClick = {
                    viewModel.setAspectRatio(Size(ar.first.toFloat(), ar.second.toFloat()))
                }) {
                    Text("${ar.first}:${ar.second}")
                }
            }

        }
    }

    @Composable
    fun ExportButton(viewModel: MainViewModel) {
        val context = LocalContext.current

        var originalFileName by remember { mutableStateOf<String?>(null) }
        var suggestedOutputUri by remember { mutableStateOf<Uri?>(null) }

        val imageUri = viewModel.inputUri.collectAsState().value

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

        val imageBitmap = viewModel.hiresBitmap.collectAsState().value

        if (imageUri != null && imageBitmap != null) {
            val exportImageLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument(
                        MIMETYPE_IMAGE
                    )
                ) { outputUri: Uri? ->
                    // TODO: handle cancellations/missing images
                    val bimap = imageBitmap
                    val uri = outputUri ?: return@rememberLauncherForActivityResult
                    viewModel.saveBitmapToUri(bitmap = bimap, uri = uri)
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
    fun ZoomableBitmap(bitmap: Bitmap, modifier: Modifier = Modifier) {
        val context = LocalContext.current
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

        val imageLoader = remember {
            ImageLoader.Builder(context)
                .bitmapConfig(Bitmap.Config.RGB_565) // Reduce memory usage
                .allowHardware(false) // Force software rendering if needed
                .build()
        }

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(bitmap)
                .size(bitmap.width, bitmap.height) // Original dimensions
                .build(),
            contentDescription = "Zoomable Image",
            imageLoader = imageLoader,
            contentScale = ContentScale.Fit, // Or other ContentScale options
            modifier = modifier
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
