
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
package com.instructure.horizon.features.account.filepreview

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.moduleitemsequence.content.file.ViewMediaActivity
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.filedetails.ImageFileContent
import com.instructure.pandautils.compose.composables.filedetails.MediaFileContent
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab

@Composable
fun FilePreview(filePreviewUiState: FilePreviewUiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.getActivityOrNull()
    Box(modifier = modifier) {
        when (filePreviewUiState) {
            is FilePreviewUiState.Image -> ImageFileContent(
                uri = filePreviewUiState.uri,
                contentDescription = filePreviewUiState.displayName,
                modifier = Modifier.fillMaxWidth(),
                loadingIndicator = { Spinner(Modifier.fillMaxSize()) }
            )

            is FilePreviewUiState.Media -> MediaFileContent(
                uri = filePreviewUiState.uri,
                contentType = filePreviewUiState.contentType,
                onFullScreenClicked = { uri, contentType ->
                    val bundle = BaseViewMediaActivity.makeBundle(
                        uri.toString(),
                        filePreviewUiState.thumbnailUrl,
                        contentType,
                        filePreviewUiState.displayName,
                        false
                    )
                    context.startActivity(ViewMediaActivity.createIntent(context, bundle))
                })

            is FilePreviewUiState.Pdf -> {
                PdfPreview(documentUri = filePreviewUiState.uri, modifier = Modifier.fillMaxSize())
            }
            is FilePreviewUiState.WebView -> {
                ComposeCanvasWebView(
                    url = filePreviewUiState.url, applyOnWebView = {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.loadWithOverviewMode = true
                        settings.displayZoomControls = false
                        settings.setSupportZoom(true)
                        activity?.let { addVideoClient(it) }
                        setInitialScale(100)
                    })
            }

            FilePreviewUiState.NoPreview -> {}
            is FilePreviewUiState.Text -> {
                ComposeCanvasWebViewWrapper(
                    content = filePreviewUiState.content,
                    contentType = filePreviewUiState.contentType,
                    useInAppFormatting = false,
                    applyOnWebView = {
                        activity?.let { addVideoClient(it) }
                        setZoomSettings(true)
                        setInitialScale(100)
                    },
                    embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                        shouldLaunchInternalWebViewFragment = { _ -> true },
                        launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilePreviewPreview() {
    ContextKeeper.appContext = LocalContext.current
    FilePreview(FilePreviewUiState.Text(
        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        contentType = "text/plain"
    ))
}