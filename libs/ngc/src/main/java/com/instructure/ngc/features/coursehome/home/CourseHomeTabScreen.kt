/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.ngc.features.coursehome.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.instui.compose.InstUITheme
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab

@Composable
fun CourseHomeTabScreen(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    viewModel: CourseHomeTabViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CourseHomeTabScreenContent(
        uiState = uiState,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun CourseHomeTabScreenContent(
    uiState: CourseHomeTabUiState,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val activity = LocalContext.current.getActivityOrNull()
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Loading(modifier = Modifier.fillMaxSize())
            return@Box
        }

        ComposeCanvasWebViewWrapper(
            content = uiState.content.orEmpty(),
            applyOnWebView = {
                // Compose's Modifier.nestedScroll does not bridge to a classic WebView, so we
                // drive the collapsing top bar's heightOffset directly from the WebView's
                // scroll position to keep it in sync with the page content.
                if (scrollBehavior != null) {
                    setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                        val delta = (scrollY - oldScrollY).toFloat()
                        val state = scrollBehavior.state
                        state.heightOffset = (state.heightOffset - delta)
                            .coerceIn(state.heightOffsetLimit, 0f)
                    }
                }
            },
            embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                shouldLaunchInternalWebViewFragment = { _ -> true },
                // TODO: Route in-app once an NGC WebView router exists; for now open
                //  embedded links in a Chrome Custom Tab.
                launchInternalWebViewFragment = { url ->
                    activity?.launchCustomTab(url, ThemePrefs.brandColor)
                },
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseHomeTabScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    InstUITheme {
        CourseHomeTabScreenContent(
            uiState = CourseHomeTabUiState(isLoading = true),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseHomeTabScreenContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    InstUITheme {
        CourseHomeTabScreenContent(
            uiState = CourseHomeTabUiState(
                isLoading = false,
                content = "Welcome to Introduction to Space Stations!",
            ),
        )
    }
}