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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.horizon.horizonui.molecules.TextLink
import com.instructure.horizon.horizonui.molecules.TextLinkIconPosition
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab

@Composable
fun ExternalToolContentScreen(uiState: ExternalToolUiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.getActivityOrNull()

    LaunchedEffect(uiState.authenticatedUrl) {
        if (!uiState.authenticatedUrl.isNullOrEmpty()) {
            activity?.launchCustomTab(uiState.authenticatedUrl, ThemePrefs.brandColor)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level5)
            .background(HorizonColors.Surface.cardPrimary()), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiState.openExternallyLoading) {
                HorizonSpace(SpaceSize.SPACE_32) // We need this space to keep the text centered when we add the loading spinner.
            }
            TextLink(
                text = stringResource(R.string.externalTool_openInANewTab),
                textLinkIconPosition = TextLinkIconPosition.End(R.drawable.open_in_new),
                modifier = modifier
                    .padding(24.dp),
                onClick = uiState.onOpenExternallyClicked
            )
            if (uiState.openExternallyLoading) {
                HorizonSpace(SpaceSize.SPACE_8)
                Spinner(size = SpinnerSize.EXTRA_SMALL)
            }
        }
        if (uiState.previewUrl.isNotEmpty() && uiState.previewState != PreviewState.ERROR) {
            Box(contentAlignment = Alignment.Center) {
                ComposeCanvasWebView(
                    uiState.previewUrl, modifier = modifier.padding(16.dp), webViewCallbacks = ComposeWebViewCallbacks(
                    onPageFinished = { _, _ -> uiState.onPageFinished() },
                    onReceivedError = { _, _, _, _ -> uiState.onPreviewError() },
                ), embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                    shouldLaunchInternalWebViewFragment = { _ -> true },
                    launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                ), applyOnWebView = {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
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