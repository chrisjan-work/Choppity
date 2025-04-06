package com.lairofpixies.choppity

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
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

    //    val viewModel: MainViewModel by viewModels()
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
//                    widthFactor = ar.first.toFloat(); heightFactor = ar.second.toFloat()
                    viewModel.setAspectRatio(Size(ar.first.toFloat(), ar.second.toFloat()))
                }) {
                    Text("${ar.first}:${ar.second}")
                }
            }

        }
    }

//    @Composable
//    fun DisplayPickedImage(imageUri: Uri?) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            if (imageUri != null) {
//                ZoomableImage(imageUri = imageUri)
//            } else {
//                // Optionally display a placeholder or message when no image is selected
//                // Example:
//                // Text("No image selected", style = MaterialTheme.typography.bodyLarge)
//            }
//        }
//    }

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
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIMETYPE_IMAGE)) { outputUri: Uri? ->
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
//
//    private fun saveImage(context: Context, inputUri: Uri, outputUri: Uri) {
//        try {
//            context.contentResolver.openInputStream(inputUri)?.use { input ->
//                context.contentResolver.openOutputStream(outputUri)?.use { output ->
//                    input.copyTo(output)
//                }
//            }
//            // Image saved successfully.  You might want to inform the user.
//        } catch (e: IOException) {
//            // Handle error (e.g., show a message)
//        }
//    }


//    @Composable
//    fun ImageOutputScreen(modifier: Modifier = Modifier) {
//        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//
//        val pickImageLauncher =
//            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//                selectedImageUri = uri
//            }
//
//        Column(modifier) {
//            Button(onClick = { pickImageLauncher.launch(MIMETYPE_IMAGE) }) {
//                Text("Pick Image")
//            }
//            //... your image display...
//
//            ImageExportScreen(imageUri = selectedImageUri)
//
////            DisplayPickedImage(imageUri = selectedImageUri)
//            BorderedImageScreen(selectedImageUri)
//
//        }
//
//    }

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

    /* v4 */

//    @Composable
//    fun BorderedImage(
//        imageUri: Uri,
//        widthFactor: Float,
//        heightFactor: Float,
//        modifier: Modifier = Modifier,
//        backgroundColor: Color = Color.Black,
//    ) {
//        val context = LocalContext.current
//        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
//        var canvasSize by remember { mutableStateOf(IntSize.Zero) }
//
//        LaunchedEffect(imageUri, widthFactor, heightFactor) {
//            imageBitmap = loadImageBitmap(context, imageUri)
//        }
//
//        if (imageBitmap != null) {
//            val originalWidth = imageBitmap!!.width.toFloat()
//            val originalHeight = imageBitmap!!.height.toFloat()
//            Box(
//                modifier = modifier
//                    .fillMaxWidth()
//                    .aspectRatio(widthFactor / heightFactor)
//                    .onSizeChanged { canvasSize = it }
//            ) {
//                val expandedCanvasWidth =
//                    floor(max(originalWidth, originalHeight * widthFactor / heightFactor))
//                val expandedCanvasHeight =
//                    floor(max(originalHeight, originalWidth * heightFactor / widthFactor))
//
//                val offsetX = (expandedCanvasWidth - originalWidth) / 2f
//                val offsetY = (expandedCanvasHeight - originalHeight) / 2f
//
////
////                val outputBitmap =
////                    ImageBitmap(expandedCanvasWidth.toInt(), expandedCanvasHeight.toInt())
////                val nativeCanvas = Canvas(outputBitmap)
////                val canvasDrawScope = CanvasDrawScope()
////                canvasDrawScope.draw(
////                    density = LocalDensity.current,
////                    layoutDirection = LayoutDirection.Ltr,
////                    canvas = nativeCanvas,
////                    size = Size(expandedCanvasWidth, expandedCanvasHeight)
////                ) {
////                    drawRect(
////                        color = backgroundColor,
////                        size = Size(expandedCanvasWidth, expandedCanvasHeight)
////                    ) // Draw background
////                    translate(left = offsetX, top = offsetY) {
////                        drawImage(
////                            image = imageBitmap!!
////                        )
////                    }
////                }
////
////                Image(outputBitmap, "result", modifier = Modifier.fillMaxSize())
////            }
//
//
//                Canvas(
//                    modifier = Modifier.scale(
//                        canvasSize.width / expandedCanvasWidth,
//                        canvasSize.height / expandedCanvasHeight
//                    )
//                ) {
//                    drawRect(
//                        color = backgroundColor,
//                        size = Size(expandedCanvasWidth, expandedCanvasHeight)
//                    ) // Draw background
//                    translate(left = offsetX, top = offsetY) {
//                        drawImage(
//                            image = imageBitmap!!
//                        )
//                    }
//                }
//            }
//        } else {
//            // Placeholder while loading
//            Text("Loading...")
//        }
//    }

//    private suspend fun loadImageBitmap(context: Context, uri: Uri): ImageBitmap? =
//        withContext(Dispatchers.IO) {
//            val loader = ImageLoader(context)
//            val request = ImageRequest.Builder(context)
//                .data(uri)
//                .build()
//
//            val result = loader.execute(request)
//            if (result is SuccessResult) {
//                result.drawable.toBitmap().asImageBitmap()
//            } else {
//                null
//            }
//        }


//    @OptIn(ExperimentalLayoutApi::class)
//    @Composable
//    fun BorderedImageScreen(imageUri: Uri?) {
//        var widthFactor by remember { mutableFloatStateOf(16f) }
//        var heightFactor by remember { mutableFloatStateOf(9f) }
//
//        Column(Modifier.fillMaxSize()) {
//            if (imageUri != null) {
//                BorderedImage(
//                    imageUri = imageUri,
//                    widthFactor = widthFactor,
//                    heightFactor = heightFactor,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f)
//                )
//
//                // Optional controls to change widthFactor and heightFactor
//                FlowRow(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    MainViewModel.ASPECT_RATIOS.forEach { ar ->
//                        Button(onClick = {
//                            widthFactor = ar.first.toFloat(); heightFactor = ar.second.toFloat()
//                        }) {
//                            Text("${ar.first}:${ar.second}")
//                        }
//                    }
//
//                }
//            } else {
//                Text("No Image Selected", modifier = Modifier.align(Alignment.CenterHorizontally))
//            }
//        }
//    }

    /* V5 */
    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createResizedBitmap(
        originalBitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
//        val resizedBitmap = createBitmap(targetWidth, targetHeight)
        val resizedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(resizedBitmap)

        // Calculate offsets to center the original image
        val offsetX = (targetWidth - originalBitmap.width) / 2f
        val offsetY = (targetHeight - originalBitmap.height) / 2f

        // Draw the original bitmap centered in the new canvas
        canvas.drawColor(Color.Black.toArgb()) // Optional: Fill background with white
        canvas.drawBitmap(originalBitmap, offsetX, offsetY, null)

        return resizedBitmap
    }

    @Composable
    fun DisplayResizedImage(bitmap: Bitmap) {
        val imageBitmap = bitmap.asImageBitmap()

        Image(
            bitmap = imageBitmap,
            contentDescription = "Resized Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }


    fun processAndDisplayImage(
        context: Context,
        inputUri: Uri,
        outputUri: Uri,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap? {
        // Step 1: Load original image
        val originalBitmap = loadBitmapFromUri(context, inputUri) ?: return null

        // Step 2: Create resized image
        val resizedBitmap = createResizedBitmap(originalBitmap, targetWidth, targetHeight)

        // Step 3: Save resized image to file
//        saveBitmapToUri(context, resizedBitmap, outputUri)

        return resizedBitmap // Return for UI display or further processing
    }


    @Composable
    fun MyImageScreen(context: Context, inputUri: Uri, outputUri: Uri) {
        var resizedImage by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(Unit) {
            resizedImage = processAndDisplayImage(context, inputUri, outputUri, 800, 600)
        }

        resizedImage?.let { DisplayResizedImage(bitmap = it) }
    }


    companion object {
        const val MIMETYPE_IMAGE = "image/*"
        const val RESET_ZOOM_ON_DOUBLETAP = true
        const val RESET_ZOOM_ON_RELEASE = false
        private const val MINIMUM_ZOOM = 1f
        private const val MAXIMUM_ZOOM = 20f

    }
}
