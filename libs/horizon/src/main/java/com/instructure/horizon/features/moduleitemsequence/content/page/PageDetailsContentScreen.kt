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
import androidx.navigation.NavHostController
import com.instructure.horizon.features.notebook.common.webview.ComposeNotesHighlightingCanvasWebView
import com.instructure.horizon.features.notebook.common.webview.NotesCallback
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.views.JSInterface

@Composable
fun PageDetailsContentScreen(
    uiState: PageDetailsUiState,
    scrollState: ScrollState,
    mainNavController: NavHostController,
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
                ComposeNotesHighlightingCanvasWebView(
                    content = it,
                    notes = uiState.notes,
                    applyOnWebView = {
                        activity?.let { addVideoClient(it) }
                        overrideHtmlFormatColors = HorizonColors.htmlFormatColors
                        if (uiState.ltiButtonPressed != null) {
                            addJavascriptInterface(JSInterface(uiState.ltiButtonPressed), Const.LTI_TOOL)
                        }
                    },
                    embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                        shouldLaunchInternalWebViewFragment = { _ -> true },
                        launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                    ),
                    notesCallback = NotesCallback(
                        onNoteSelected = { noteId, noteType, selectedText, userComment, startContainer, startOffset, endContainer, endOffset ->
                            mainNavController.navigate(
                                MainNavigationRoute.EditNotebook(
                                    noteId = noteId,
                                    noteType = noteType,
                                    highlightedTextStartOffset = startOffset,
                                    highlightedTextEndOffset = endOffset,
                                    highlightedTextStartContainer = startContainer,
                                    highlightedTextEndContainer = endContainer,
                                    highlightedText = selectedText,
                                    userComment = userComment
                                )
                            )
                        },
                        onNoteAdded = { selectedText, startContainer, startOffset, endContainer, endOffset ->
                            mainNavController.navigate(
                                MainNavigationRoute.AddNotebook(
                                    courseId = uiState.courseId.toString(),
                                    objectType = "Page",
                                    objectId = uiState.pageId.toString(),
                                    highlightedTextStartOffset = startOffset,
                                    highlightedTextEndOffset = endOffset,
                                    highlightedTextStartContainer = startContainer,
                                    highlightedTextEndContainer = endContainer,
                                    highlightedText = selectedText
                                )
                            )
                        }
                    )
                )
                HorizonSpace(SpaceSize.SPACE_48)
            }
        }
    }
}
