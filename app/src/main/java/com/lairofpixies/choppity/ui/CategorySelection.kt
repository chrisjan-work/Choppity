package com.lairofpixies.choppity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lairofpixies.choppity.R
import com.lairofpixies.choppity.data.AspectRatio


@Composable
fun CategoryButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    BaseButton(
        modifier = modifier.size(96.dp, 56.dp),
        onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                content()
            }
            Text(label.uppercase(), fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
fun AspectRatioCategoryButton(
    aspectRatio: AspectRatio,
    selectCategory: (OptionCategories) -> Unit,
) {
    val label = stringResource(R.string.aspect_ratio)
    CategoryButton(
        modifier = Modifier.semantics { contentDescription = "$label is ${aspectRatio.readable}" },
        label = label,
        onClick = { selectCategory(OptionCategories.ASPECT_RATIO) }) {
        Text(text = aspectRatio.label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
fun CategoryButtonsPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AspectRatioCategoryButton(AspectRatio.Square) { }
        BarColorCategoryButton(Color(0xFF000000)) { }
        SectionCountCategoryButton(3) { }
    }
}

@Composable
fun BarColorCategoryButton(
    barColor: Color,
    selectCategory: (OptionCategories) -> Unit
) {
    CategoryButton(
        // modifier = Modifier.semantics { contentDescription = TODO() },
        label =  stringResource(R.string.bar_color),
        onClick = {
            selectCategory(OptionCategories.FILL_COLOR)
        }) {
        BarColorBox(barColor)
    }
}

@Composable
fun SectionCountCategoryButton(
    sectionCount: Int,
    selectCategory: (OptionCategories) -> Unit
) {
    val label = stringResource(R.string.sections)
    CategoryButton(
        modifier = Modifier.semantics { contentDescription = "$sectionCount $label" },
        label = label,
        onClick = {
            selectCategory(OptionCategories.SECTION_COUNT)
        }) {
        Text(text = sectionCount.toString())
    }
}