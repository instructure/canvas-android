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
package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import com.instructure.pandautils.views.CanvasWebView

@Composable
fun ComposeCanvasWebView(
    url: String,
    modifier: Modifier = Modifier,
    applyOnWebView: (CanvasWebView.() -> Unit)? = null
) {
    val webViewState = rememberSaveable { bundleOf() }

    if (LocalInspectionMode.current) {
        Text(text = url)
    } else {
        AndroidView(
            factory = {
                CanvasWebView(it).apply {
                    applyOnWebView?.let { applyOnWebView -> applyOnWebView() }
                }
            },
            update = {
                if (webViewState.isEmpty) {
                    it.loadUrl(url)
                } else {
                    it.restoreState(webViewState)
                }
            },
            onRelease = {
                it.saveState(webViewState)
            },
            modifier = modifier.fillMaxSize()
        )
    }
}