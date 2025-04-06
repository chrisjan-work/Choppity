package com.lairofpixies.choppity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
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
import com.lairofpixies.choppity.logic.MainViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(
    viewModel: MainViewModel,
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
