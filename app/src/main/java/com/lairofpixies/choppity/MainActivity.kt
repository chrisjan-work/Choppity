package com.lairofpixies.choppity

import android.content.Intent
import android.net.Uri
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

        parseIntent(intent)

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
    fun MainScreen(modifier: Modifier = Modifier) {
        Column(modifier.fillMaxSize()) {
            // Input
            val inputUri = viewModel.inputUri.collectAsState()
            val hiresBitmap = viewModel.hiresBitmap.collectAsState()
            ActionRow(
                inputUri = inputUri.value,
                outputAvailable = hiresBitmap.value != null,
                importAction = { uri -> viewModel.importImage(uri) },
                exportAction = { uri ->
                    hiresBitmap.value?.let { bitmap ->
                        viewModel.saveBitmapToUri(bitmap, uri)
                    }
                },
                rotateAction = { viewModel.increaseRotation() }
            )
            // image
            val loresBitmap = viewModel.loresBitmap.collectAsState()
            ProcessedImageDisplay(loresBitmap.value, modifier = Modifier.weight(1f))
            // aspect ratio
            OptionsRow(
                setAspectRatio = { aspectRatio -> viewModel.setAspectRatio(aspectRatio) },
                setColor = { color -> viewModel.setColor(color) }
            )
        }
    }

}
