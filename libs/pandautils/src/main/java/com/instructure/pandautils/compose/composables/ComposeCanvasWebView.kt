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

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.pandautils.views.CanvasWebView

data class ComposeWebViewCallbacks(
    val openMedia: (String, String, String) -> Unit = { _, _, _ -> },
    val onPageFinished: (WebView, String) -> Unit = { _, _ -> },
    val onPageStarted: (WebView, String) -> Unit = { _, _ -> },
    val canRouteInternally: (String) -> Boolean = { _ -> false },
    val routeInternally: (String) -> Unit = { _ -> },
    val onReceivedError: (WebView, Int, String, String) -> Unit = { _, _, _, _ -> },
)

data class ComposeEmbeddedWebViewCallbacks(
    val shouldLaunchInternalWebViewFragment: (String) -> Boolean = { _ -> false },
    val launchInternalWebViewFragment: (String) -> Unit = { _ -> }
)

@Composable
fun ComposeCanvasWebView(
    url: String,
    modifier: Modifier = Modifier,
    webViewCallbacks: ComposeWebViewCallbacks = ComposeWebViewCallbacks(),
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(),
    applyOnWebView: (CanvasWebView.() -> Unit)? = null
) {
    val stateViewModel: ComposeCanvasWebViewStateViewModel = viewModel()
    val webViewState = stateViewModel.webViewState

    if (LocalInspectionMode.current) {
        Text(text = url)
    } else {
        AndroidView(
            factory = {
                CanvasWebView(it).apply {
                    canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                        override fun openMediaFromWebView(mime: String, url: String, filename: String) =
                            webViewCallbacks.openMedia(mime, url, filename)

                        override fun onPageFinishedCallback(webView: WebView, url: String) = webViewCallbacks.onPageFinished(webView, url)

                        override fun onPageStartedCallback(webView: WebView, url: String) = webViewCallbacks.onPageStarted(webView, url)

                        override fun canRouteInternallyDelegate(url: String): Boolean = webViewCallbacks.canRouteInternally(url)

                        override fun routeInternallyCallback(url: String) = webViewCallbacks.routeInternally(url)

                        override fun onReceivedErrorCallback(webView: WebView, errorCode: Int, description: String, failingUrl: String) =
                            webViewCallbacks.onReceivedError(webView, errorCode, description, failingUrl)
                    }
                    canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                        override fun launchInternalWebViewFragment(url: String) =
                            embeddedWebViewCallbacks.launchInternalWebViewFragment(url)

                        override fun shouldLaunchInternalWebViewFragment(url: String): Boolean =
                            embeddedWebViewCallbacks.shouldLaunchInternalWebViewFragment(url)
                    }

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