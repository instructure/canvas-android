/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.smartsearch

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.SearchBar
import com.instructure.pandautils.utils.color

@Composable
fun SmartSearchScreen(uiState: SmartSearchUiState) {
    CanvasTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = Color(uiState.canvasContext.color),
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                painterResource(id = R.drawable.ic_back_arrow),
                                contentDescription = stringResource(R.string.contentDescription_back),
                                tint = colorResource(R.color.textLightest)
                            )
                        }
                    },
                    title = {
                        SearchBar(
                            icon = R.drawable.ic_smart_search,
                            tintColor = colorResource(R.color.textLightest),
                            onExpand = {},
                            onSearch = {},
                            placeholder = "Search in this course",
                            collapsable = false,
                            searchQuery = uiState.query
                        )
                    },
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(colorResource(R.color.backgroundLightest))
            ) { }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun SmartSearchPreview() {
    SmartSearchScreen(
        SmartSearchUiState(
            "query",
            CanvasContext.defaultCanvasContext(),
            emptyList()
        ) {})
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartSearchDarkPreview() {
    SmartSearchScreen(
        SmartSearchUiState(
            "query",
            CanvasContext.defaultCanvasContext(),
            emptyList()
        ) {})
}
