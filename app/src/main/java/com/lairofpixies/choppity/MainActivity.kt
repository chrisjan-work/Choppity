package com.lairofpixies.choppity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lairofpixies.choppity.logic.DiskLogic
import com.lairofpixies.choppity.logic.MainViewModel
import com.lairofpixies.choppity.ui.OptionsRow
import com.lairofpixies.choppity.ui.ProcessedImageDisplay
import com.lairofpixies.choppity.ui.ProgressDialog
import com.lairofpixies.choppity.ui.ScreenDimensionsUpdater
import com.lairofpixies.choppity.ui.theme.ChoppityTheme


class MainActivity : ComponentActivity() {

    // poor mans dependency injection
    val diskLogic = DiskLogic(this)
    val viewModel = MainViewModel(diskLogic)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parseIntent(intent)

        enableEdgeToEdge()
        setContent {
            ChoppityTheme {
                MainScreen()
            }
        }
    }


    private fun parseIntent(newIntent: Intent?) {
        if (newIntent?.action in setOf(Intent.ACTION_SEND, Intent.ACTION_EDIT)) {
            val importedUri =
                newIntent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java) ?: return
            viewModel.importImage(importedUri)
        }
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        parseIntent(newIntent)
    }

    @Composable
    fun importCallbackFactory(): () -> Unit {
        var inputUri by remember { mutableStateOf<Uri?>(null) }

        val pickImageLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { inputUri = it }
            }

        LaunchedEffect(inputUri) {
            val uri = inputUri ?: return@LaunchedEffect
            viewModel.importImage(uri)
        }

        return { pickImageLauncher.launch(Constants.MIMETYPE_IMAGE) }
    }

    @Composable
    fun exportCallbackFactory(): (Bitmap, String) -> Unit {
        var outputUri by remember { mutableStateOf<Uri?>(null) }
        var partialBitmap by remember { mutableStateOf<Bitmap?>(null) }

        val exportRequest = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(
                Constants.MIMETYPE_IMAGE
            )
        ) { selectedUri: Uri? ->
            selectedUri?.let { outputUri = selectedUri }
        }

        LaunchedEffect(outputUri) {
            val exportUri = outputUri ?: return@LaunchedEffect
            val exportBitmap = partialBitmap ?: return@LaunchedEffect
            viewModel.exportSingle(exportBitmap, exportUri)
        }

        val callForOutput = { bitmap: Bitmap, suggestedFilename: String ->
            partialBitmap = bitmap
            exportRequest.launch(suggestedFilename)
        }
        return callForOutput
    }


    @Composable
    fun MainScreen() {
        MaterialTheme.colorScheme.background.let { initialBackground ->
            LaunchedEffect(initialBackground) {
                viewModel.updateAppBackgroundColor(initialBackground)
            }
        }
        Scaffold(
            topBar = { MainTopBar() },
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            ScreenDimensionsUpdater { screenSize -> viewModel.updateScreenSize(screenSize) }
            CentralView(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(viewModel.appBackground.collectAsState().value)
            )
        }
    }

    @Composable
    fun CentralView(modifier: Modifier = Modifier) {
        Column(modifier.fillMaxSize()) {
            // image
            val loresBitmap = viewModel.loresBitmap.collectAsState()
            ProcessedImageDisplay(loresBitmap.value, modifier = Modifier.weight(1f))
            // aspect ratio
            OptionsRow(
                setAspectRatio = { aspectRatio -> viewModel.setAspectRatio(aspectRatio) },
                setColor = { color -> viewModel.setRenderColor(color) },
                setSections = { separators -> viewModel.setSections(separators) }
            )
        }

        val showBusy = viewModel.busyIndicator.collectAsState().value
        if (showBusy) {
            ProgressDialog(modifier = modifier) {
                viewModel.toggleBusy(false)
            }
        }
    }

    @Composable
    fun MainTopBar() {
        val callForInput = importCallbackFactory()
        val callForOutput = exportCallbackFactory()
        val hiresBitmap = viewModel.hiresBitmap.collectAsState()

        // for flipaction
        val normalColor = MaterialTheme.colorScheme.background
        val flippedColor = MaterialTheme.colorScheme.onBackground
        val currentColor = viewModel.appBackground.collectAsState()
        LaunchedEffect(Unit) {
            viewModel.updateAppBackgroundColor(normalColor)
        }

        ChopTopBar(
            outputAvailable = hiresBitmap.value != null,
            importAction = { callForInput() },
            exportAction = {
                hiresBitmap.value?.let { bitmap ->
                    viewModel.launchExports(bitmap, callForOutput)
                }
            },
            rotateAction = { viewModel.increaseRotation() },
            flipAction = {
                viewModel.updateAppBackgroundColor(
                    if (currentColor.value == normalColor) {
                        flippedColor
                    } else {
                        normalColor
                    }
                )
            }
        )
    }
}
