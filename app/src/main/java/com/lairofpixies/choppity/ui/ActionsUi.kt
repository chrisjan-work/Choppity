package com.lairofpixies.choppity.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.lairofpixies.choppity.Constants


@Composable
fun ActionRow(
    inputUri: Uri?,
    outputAvailable: Boolean,
    flipAppColor: (Color) -> Unit,
    importAction: (Uri) -> Unit,
    exportAction: () -> Unit,
    rotateAction: () -> Unit,
) {
    Column {
        Row {
            ImportButton { newUri ->
                importAction(newUri)
            }

            if (inputUri != null && outputAvailable) {
                ExportButton(exportAction)
            }
        }

        Row {
            BackgroundAppButton { newColor -> flipAppColor(newColor) }
            if (inputUri != null && outputAvailable) {
                RotateButton(rotateAction)
            }
        }
    }
}

@Composable
fun ImportButton(uriSelected: (uri: Uri) -> Unit) {
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uriSelected(it) }
        }

    Button(onClick = { pickImageLauncher.launch(Constants.MIMETYPE_IMAGE) }) {
        Text("Pick Image")
    }
}

@Composable
fun ExportButton(launchExport: () -> Unit) {
    Button(onClick = launchExport) {
        Text("Export")
    }
}

@Composable
fun RotateButton(rotateAction: () -> Unit) {
    Button(onClick = rotateAction) {
        Text("Rotate")
    }
}

@Composable
fun BackgroundAppButton(flipAppColor: (Color) -> Unit) {
    val acceptedColors = listOf(
        "System" to MaterialTheme.colorScheme.background,
        "Invert" to MaterialTheme.colorScheme.onBackground
    )
    var currentColor by remember { mutableIntStateOf(0) }
    Button(onClick = {
        currentColor = 1 - currentColor
        flipAppColor(acceptedColors[currentColor].second)
    }) {
        Text(text = acceptedColors[currentColor].first)
    }
}