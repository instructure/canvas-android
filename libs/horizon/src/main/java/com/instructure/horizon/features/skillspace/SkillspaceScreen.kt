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

import android.content.Intent
import android.graphics.Color
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.util.bottomNavigationScreenInsets
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.views.CanvasWebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillspaceScreen(state: SkillspaceUiState) {
    val activity = LocalContext.current.getActivityOrNull()
    var webView: CanvasWebView? by remember { mutableStateOf(null) }

    var request by remember { mutableIntStateOf(0) }
    val launchPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        webView?.handleOnActivityResult(request, result.resultCode, result.data)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.bottomNavigationScreenInsets)
    ) {
        LoadingStateWrapper(state.loadingState) {
            state.webviewUrl?.let {
                ComposeCanvasWebView(
                    url = state.webviewUrl,
                    embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                        shouldLaunchInternalWebViewFragment = { url -> !url.contains("/login") && !url.contains("/oauth") },
                        launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                    ),
                    applyOnWebView = {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(Color.TRANSPARENT)
                        setInitialScale(100)
                        activity?.let { addVideoClient(activity) }
                        setCanvasWebChromeClientShowFilePickerCallback(object: CanvasWebView.VideoPickerCallback {
                            override fun requestStartActivityForResult(
                                intent: Intent,
                                requestCode: Int
                            ) {
                                request = requestCode
                                launchPicker.launch(intent)
                            }

                            override fun permissionsGranted(): Boolean = true
                        })
                        webView = this
                    }
                )
            }
        }
    }
}