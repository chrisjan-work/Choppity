/*
 * This file is part of Choppity.
 *
 * Copyright (C) 2025 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.lairofpixies.choppity.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lairofpixies.choppity.data.ProcessParams
import com.lairofpixies.choppity.logic.MainViewModel
import kotlinx.coroutines.CoroutineScope
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

    BottomSheetScaffold(
        topBar = topBar,
        scaffoldState = scaffoldState,
        sheetContent = { BottomSheet(viewModel, scope, scaffoldState, extraBottomPeek) },
        sheetPeekHeight = extraBottomPeek + 80.dp,
        sheetSwipeEnabled = true,
        sheetDragHandle = { /* Empty drag handle */ },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = modifier,
        content = content
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BottomSheet(
    viewModel: MainViewModel,
    scope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState,
    extraBottomPeek: Dp
) {
    var optionCategory by remember { mutableStateOf(OptionCategories.ASPECT_RATIO) }
    val optionButtonsVisible =
        scaffoldState.bottomSheetState.currentValue != SheetValue.PartiallyExpanded

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // chooser
        CategoryChooserRow(viewModel) { category ->
            optionCategory = category
            scope.launch { scaffoldState.bottomSheetState.expand() }
        }

        Spacer(Modifier.height(12.dp))

        CategoryOptionsBox(optionCategory, optionButtonsVisible, viewModel)

        Spacer(Modifier.height(extraBottomPeek))
    }
}

@Composable
fun CategoryChooserRow(viewModel: MainViewModel?, selectCategory: (OptionCategories) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val params = viewModel?.processParams?.collectAsState()?.value ?: ProcessParams.Default
        AspectRatioCategoryButton(params.aspectRatio, selectCategory)
        FillColorCategoryButton(params.bgColor, selectCategory)
        SectionCountCategoryButton(params.sectionCount, selectCategory)
    }
}

@Preview
@Composable
fun CategoryChooserRowPreview() {
    CategoryChooserRow(null) { }
}

@Composable
fun CategoryOptionsBox(
    optionCategory: OptionCategories,
    optionButtonsVisible: Boolean,
    viewModel: MainViewModel?
) {
    // button views
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (optionButtonsVisible) 1.0f else 0.0f)
    ) {
        when (optionCategory) {
            OptionCategories.ASPECT_RATIO -> AspectRatioRow(
                setAspectRatio = { aspectRatio -> viewModel?.setAspectRatio(aspectRatio) }
            )

            OptionCategories.FILL_COLOR -> ColorRow(
                setColor = { color -> viewModel?.setRenderColor(color) }
            )

            OptionCategories.SECTION_COUNT -> SectionRow(
                setSections = { sections -> viewModel?.setSections(sections) }
            )
        }
    }
}

@Preview
@Composable
fun CategoryOptionsBoxPreview() {
    CategoryOptionsBox(OptionCategories.ASPECT_RATIO, true, null)
}
