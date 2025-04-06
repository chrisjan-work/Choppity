package com.lairofpixies.choppity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.lairofpixies.choppity.ui.ActionRow
import com.lairofpixies.choppity.ui.OptionsRow
import com.lairofpixies.choppity.ui.ProcessedImageDisplay
import com.lairofpixies.choppity.ui.ScreenDimensionsUpdater
import com.lairofpixies.choppity.ui.theme.ChoppityTheme


class MainActivity : ComponentActivity() {

    val viewModel = MainViewModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChoppityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScreenDimensionsUpdater { screenSize -> viewModel.updateScreenSize(screenSize) }
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {
        Column(modifier.fillMaxSize()) {
            // Input
            val bitmap = viewModel.hiresBitmap.collectAsState().value
            ActionRow(
                outputAvailable = bitmap != null,
                importAction = { uri -> viewModel.importImage(uri) },
                exportAction = { uri -> bitmap?.let { viewModel.saveBitmapToUri(bitmap, uri) } }
            )
            // image
            val loresBitmap = viewModel.loresBitmap.collectAsState().value
            ProcessedImageDisplay(loresBitmap, modifier = Modifier.weight(1f))
            // aspect ratio
            OptionsRow { aspectRatio -> viewModel.setAspectRatio(aspectRatio) }
        }
    }

}
