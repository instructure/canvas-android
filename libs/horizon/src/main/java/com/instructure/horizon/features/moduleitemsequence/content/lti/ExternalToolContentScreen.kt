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
package com.instructure.horizon.features.moduleitemsequence.content.lti

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.TextLink
import com.instructure.horizon.horizonui.molecules.TextLinkIconPosition
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.views.CanvasWebView

@Composable
fun ExternalToolContentScreen(uiState: ExternalToolUiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.getActivityOrNull()
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level5)
            .background(HorizonColors.Surface.cardPrimary()), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextLink(
            text = stringResource(R.string.externalTool_openInANewTab),
            textLinkIconPosition = TextLinkIconPosition.End(R.drawable.open_in_new),
            modifier = modifier
                .padding(24.dp)
        ) {
            activity?.launchCustomTab(uiState.urlToOpen, ThemePrefs.brandColor)
        }
        if (uiState.previewUrl.isNotEmpty() && uiState.previewState != PreviewState.ERROR) {
            Box(contentAlignment = Alignment.Center) {
                ComposeCanvasWebView(uiState.previewUrl, modifier = modifier.padding(16.dp), applyOnWebView = {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                        override fun openMediaFromWebView(mime: String, url: String, filename: String) = Unit

                        override fun onPageFinishedCallback(webView: WebView, url: String) {
                            uiState.onPageFinished()
                        }

                        override fun onPageStartedCallback(webView: WebView, url: String) = Unit

                        override fun canRouteInternallyDelegate(url: String): Boolean = false

                        override fun routeInternallyCallback(url: String) = Unit

                        override fun onReceivedErrorCallback(webView: WebView, errorCode: Int, description: String, failingUrl: String) {
                            uiState.onPreviewError()
                        }
                    }
                    canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                        override fun launchInternalWebViewFragment(url: String) {
                            activity?.launchCustomTab(url, ThemePrefs.brandColor)
                        }

                        override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
                    }
                    setInitialScale(100)
                    activity?.let { addVideoClient(it) }
                })
                if (uiState.previewState == PreviewState.LOADING) {
                    Spinner(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
@Preview
fun ExternalLinkContentScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val uiState = ExternalToolUiState(
        previewUrl = "https://example.com",
        urlToOpen = "https://example.com"
    )
    ExternalToolContentScreen(uiState)
}