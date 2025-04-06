package com.lairofpixies.choppity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import com.lairofpixies.choppity.Constants


@Composable
fun OptionsRow(setAspectRatio: (Size) -> Unit) {
    Column {
        AspectRatioRow(setAspectRatio)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AspectRatioRow(
    setAspectRatio: (Size) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Constants.ASPECT_RATIOS.forEach { ar ->
            Button(onClick = {
                setAspectRatio(Size(ar.first.toFloat(), ar.second.toFloat()))
            }) {
                Text("${ar.first}:${ar.second}")
            }
        }

    }
}