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

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.instructure.horizon.features.notebook.common.js.JSTextSelectionInterface.Companion.addTextSelectionInterface
import com.instructure.horizon.features.notebook.common.js.JSTextSelectionInterface.Companion.highlightNotes
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.views.JSInterface

@Composable
fun PageDetailsContentScreen(
    uiState: PageDetailsUiState,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(uiState.urlToOpen) {
        uiState.urlToOpen?.let { url ->
            activity?.launchCustomTab(url, ThemePrefs.brandColor)
            uiState.onUrlOpened()
        }
    }
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
                    content = it,
                    applyOnWebView = {
                        activity?.let { addVideoClient(it) }
                        overrideHtmlFormatColors = HorizonColors.htmlFormatColors
                        if (uiState.ltiButtonPressed != null) {
                            addJavascriptInterface(JSInterface(uiState.ltiButtonPressed), Const.LTI_TOOL)
                        }
                        addTextSelectionInterface(
                            onTextSelect = { Log.d("PageDetailsContentScreen", "Text selected: $it") },
                            onHighlightedTextClick = { Log.d("PageDetailsContentScreen", "Note clicked: $it") }
                        )
                    },
                    embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                        shouldLaunchInternalWebViewFragment = { _ -> true },
                        launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                    ),
                    webViewCallbacks = ComposeWebViewCallbacks(
                        onPageFinished = { webView, _ ->
                            webView.addTextSelectionInterface(
                                onTextSelect = { Log.d("PageDetailsContentScreen", "Text selected: $it") },
                                onHighlightedTextClick = { Log.d("PageDetailsContentScreen", "Note clicked: $it") }
                            )

                            webView.highlightNotes(uiState.notes)
                        }
                    )
                )
                HorizonSpace(SpaceSize.SPACE_48)
            }
        }
    }
}
