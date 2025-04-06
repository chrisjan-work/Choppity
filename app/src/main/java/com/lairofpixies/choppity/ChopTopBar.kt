package com.lairofpixies.choppity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun ChopTopBar(
    modifier: Modifier = Modifier,
    outputAvailable: Boolean,
    importAction: () -> Unit,
    exportAction: () -> Unit
) {
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .statusBarsPadding(),
        content = {
            Row(
                Modifier
                    .align(Alignment.CenterEnd),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Import",
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .clickable { importAction() },
                    tint = MaterialTheme.colorScheme.onBackground,
                )

                val exportModifier = if (outputAvailable) {
                    Modifier
                        .padding(8.dp)
                        .clickable { exportAction() }
                } else {
                    Modifier
                        .padding(8.dp)
                        .alpha(0.2f)
                }

                Icon(
                    Icons.Default.Done,
                    contentDescription = "Export",
                    modifier = exportModifier,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    )
}