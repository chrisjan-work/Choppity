package com.lairofpixies.choppity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.lairofpixies.choppity.logic.MainViewModel


@Composable
fun MainScreen(viewModel: MainViewModel) {
    MaterialTheme.colorScheme.background.let { initialBackground ->
        LaunchedEffect(initialBackground) {
            viewModel.updateAppBackgroundColor(initialBackground)
        }
    }

    MainScreenScaffold(
        viewModel,
        topBar = { MainTopBar(viewModel) },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        ScreenDimensionsUpdater { screenSize -> viewModel.updateScreenSize(screenSize) }
        CentralView(
            viewModel,
            modifier = Modifier
                .padding(innerPadding)
                .background(viewModel.appBackground.collectAsState().value)
        )
    }
}

@Composable
fun CentralView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        // image
        val loresBitmap = viewModel.loresBitmap.collectAsState()
        ProcessedImageDisplay(loresBitmap.value, modifier = Modifier.weight(1f))
    }

    val showBusy = viewModel.busyIndicator.collectAsState().value
    if (showBusy) {
        ProgressDialog(modifier = modifier) {
            viewModel.toggleBusy(false)
        }
    }
}