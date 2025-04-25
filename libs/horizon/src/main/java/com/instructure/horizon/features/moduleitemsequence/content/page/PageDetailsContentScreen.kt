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
package com.instructure.horizon.features.moduleitemsequence.content.page

import android.webkit.WebView
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.views.CanvasWebView

@Composable
fun PageDetailsContentScreen(
    uiState: PageDetailsUiState,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    uiState.pageHtmlContent?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .clip(HorizonCornerRadius.level5)
                .background(HorizonColors.Surface.cardPrimary())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                ComposeCanvasWebViewWrapper(
                    html = it,
                    applyOnWebView = {
                        canvasEmbeddedWebViewCallback = embeddedWebViewCallback
                        canvasWebViewClientCallback = webViewClientCallback
                        overrideHtmlFormatColors = HorizonColors.htmlFormatColors
                    }
                )
                HorizonSpace(SpaceSize.SPACE_48)
            }
        }
    }

}

private val embeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
    override fun launchInternalWebViewFragment(url: String) = Unit

    override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
}

private val webViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
    override fun openMediaFromWebView(mime: String, url: String, filename: String) = Unit

    override fun onPageStartedCallback(webView: WebView, url: String) = Unit

    override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

    override fun canRouteInternallyDelegate(url: String) = false

    override fun routeInternallyCallback(url: String) = Unit
}
