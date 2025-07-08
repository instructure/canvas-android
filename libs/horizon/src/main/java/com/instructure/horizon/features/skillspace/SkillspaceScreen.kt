/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.skillspace

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillspaceScreen(state: SkillspaceUiState, mainNavController: NavHostController) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var webViewCanNavigateBack by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        LoadingStateWrapper(state.loadingState) {
            state.webviewUrl?.let {
                ComposeCanvasWebView(
                    url = state.webviewUrl,
                    webViewCallbacks = ComposeWebViewCallbacks(
                        onPageStarted = { view, _ ->
                            webView = view
                            webViewCanNavigateBack = view.canGoBack()
                        },
                        canRouteInternally = { url ->
                            url.contains("/learn") && url.toUri().lastPathSegment?.toLongOrNull() != null
                        },
                        routeInternally = { url ->
                            val courseId = url.toUri().lastPathSegment?.toLongOrNull()
                            val learnRoute = HomeNavigationRoute.Learn.withArgs(courseId)
                            mainNavController.navigate(learnRoute)
                        },
                    ),
                    applyOnWebView = {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(Color.TRANSPARENT)
                        setInitialScale(100)
                        webView = this
                    }
                )
            }
        }
    }

    BackHandler(enabled = webViewCanNavigateBack) {
        webView?.goBack()
    }
}