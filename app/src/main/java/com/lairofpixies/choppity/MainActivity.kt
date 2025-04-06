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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lairofpixies.choppity.logic.DiskLogic
import com.lairofpixies.choppity.logic.MainViewModel
import com.lairofpixies.choppity.ui.AspectRatioRow
import com.lairofpixies.choppity.ui.ColorRow
import com.lairofpixies.choppity.ui.OptionCategories
import com.lairofpixies.choppity.ui.ProcessedImageDisplay
import com.lairofpixies.choppity.ui.ProgressDialog
import com.lairofpixies.choppity.ui.ScreenDimensionsUpdater
import com.lairofpixies.choppity.ui.SectionRow
import com.lairofpixies.choppity.ui.theme.ChoppityTheme
import kotlinx.coroutines.launch


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

        MainScreenScaffold(
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
//            OptionsRow(
//                setAspectRatio = { aspectRatio -> viewModel.setAspectRatio(aspectRatio) },
//                setColor = { color -> viewModel.setRenderColor(color) },
//                setSections = { separators -> viewModel.setSections(separators) }
//            )

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreenScaffold(
        topBar: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable (PaddingValues) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded
            )
        )

        val extraBottomPeek = with(LocalDensity.current) {
            WindowInsets.navigationBars.getBottom(LocalDensity.current).toDp()
        }

        var optionsChoice by remember { mutableStateOf(OptionCategories.ASPECT_RATIO) }

        BottomSheetScaffold(
            topBar = topBar,
            scaffoldState = scaffoldState,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // chooser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val params = viewModel.processParams.collectAsState().value
                        Button(onClick = {
                            optionsChoice = OptionCategories.ASPECT_RATIO
                            scope.launch { scaffoldState.bottomSheetState.expand() }
                        }) {
                            params.aspectRatio.let {
                                val text = if (it.width <= 0f || it.height <= 0f) {
                                    "Orig"
                                } else {
                                    "${it.width.toInt()}:${it.height.toInt()}"
                                }
                                Text(text)
                            }
                        }
                        Button(onClick = {
                            optionsChoice = OptionCategories.FILL_COLOR
                            scope.launch { scaffoldState.bottomSheetState.expand() }
                        }) {
                            Box(
                                Modifier
                                    .size(36.dp, 24.dp)
                                    .background(params.bgColor)
                            )
                        }
                        Button(onClick = {
                            optionsChoice = OptionCategories.SECTION_COUNT
                            scope.launch { scaffoldState.bottomSheetState.expand() }
                        }) {
                            Text(text = params.sectionCount.toString())
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // button views
                    val optionButtonsAlpha =
                        if (scaffoldState.bottomSheetState.currentValue != SheetValue.PartiallyExpanded) {
                            1.0f
                        } else {
                            0.0f
                        }
                    when (optionsChoice) {
                        OptionCategories.ASPECT_RATIO -> AspectRatioRow(
                            modifier = Modifier.alpha(optionButtonsAlpha),
                            setAspectRatio = { aspectRatio ->
                                viewModel.setAspectRatio(
                                    aspectRatio
                                )
                            }
                        )

                        OptionCategories.FILL_COLOR -> ColorRow(
                            modifier = Modifier.alpha(optionButtonsAlpha),
                            setColor = { color -> viewModel.setRenderColor(color) }
                        )

                        OptionCategories.SECTION_COUNT -> SectionRow(
                            modifier = Modifier.alpha(optionButtonsAlpha),
                            setSections = { sections -> viewModel.setSections(sections) }
                        )
                    }
                    Spacer(Modifier.height(extraBottomPeek))
                }
            },
            sheetPeekHeight = extraBottomPeek + 80.dp,
            sheetSwipeEnabled = true,
            sheetDragHandle = { /* Empty drag handle */ },
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = modifier,
            content = content
        )
    }
}
