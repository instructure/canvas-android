/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.learn.overview

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.views.CanvasWebView

@Composable
fun LearnOverviewScreen(
    summaryText: String?,
    modifier: Modifier = Modifier,
) {
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            ComposeCanvasWebViewWrapper(
                html = summaryText ?: "",
                applyOnWebView = {
                    canvasEmbeddedWebViewCallback = embeddedWebViewCallback
                    canvasWebViewClientCallback = webViewClientCallback
                    overrideHtmlFormatColors = HorizonColors.htmlFormatColors
                },
            )
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
