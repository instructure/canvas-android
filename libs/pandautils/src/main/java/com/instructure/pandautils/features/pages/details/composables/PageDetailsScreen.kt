/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.pages.details.composables

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.notebookPen
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.features.notes.notebook.NotebookTray
import com.instructure.pandautils.features.notes.webview.JSTextSelectionInterface.Companion.addTextSelectionInterface
import com.instructure.pandautils.features.notes.webview.JSTextSelectionInterface.Companion.evaluateTextSelectionInterface
import com.instructure.pandautils.features.notes.webview.JSTextSelectionInterface.Companion.highlightRanges
import com.instructure.pandautils.features.notes.webview.toWebViewHighlight
import com.instructure.pandautils.features.pages.details.PageDetailsScreenState
import com.instructure.pandautils.features.pages.details.PageDetailsUiState
import com.instructure.pandautils.features.studytools.StudyToolsPanel
import com.instructure.pandautils.features.studytools.StudyToolsUiState
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.CanvasWebViewWrapper

/**
 * [studyToolsUiState] is resolved by the caller (its owning `StudyToolsViewModel` is
 * Fragment-scoped) rather than via `hiltViewModel()` here, so this composable stays testable
 * under a plain `createComposeRule()` — `:pandautils` androidTest has no Hilt harness.
 */
@Composable
fun PageDetailsScreen(
    uiState: PageDetailsUiState,
    @ColorInt toolbarColor: Int,
    navigationActionClick: () -> Unit,
    webViewCallbacks: ComposeWebViewCallbacks,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks,
    modifier: Modifier = Modifier,
    studyToolsUiState: StudyToolsUiState = StudyToolsUiState(),
    applyOnWebView: (CanvasWebView.() -> Unit)? = null,
    applyOnUpdate: (CanvasWebViewWrapper.() -> Unit)? = null,
    onAddBookmark: (() -> Unit)? = null
) {
    CanvasTheme {
        CanvasScaffold(
            modifier = modifier.testTag("pageDetailsPage"),
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = uiState.toolbarTitle,
                    navigationActionClick = navigationActionClick,
                    backgroundColor = Color(toolbarColor),
                    contentColor = colorResource(id = R.color.textLightest),
                    actions = {
                        if (studyToolsUiState.available) {
                            IconButton(
                                onClick = studyToolsUiState.actions.onOpen,
                                modifier = Modifier.testTag("studyToolsButton")
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_study_tools),
                                    contentDescription = stringResource(id = R.string.studyTools_openContentDescription),
                                    tint = colorResource(id = R.color.textLightest)
                                )
                            }
                        }
                        if (uiState.editMenuVisible) {
                            IconButton(
                                onClick = uiState.actions.onEditClick,
                                modifier = Modifier.testTag("editPageButton")
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = stringResource(id = R.string.edit),
                                    tint = colorResource(id = R.color.textLightest)
                                )
                            }
                        }
                        if (uiState.notebookButtonVisible) {
                            IconButton(onClick = uiState.actions.onNotebookClick, modifier = Modifier.testTag("notebookButton")) {
                                Icon(
                                    imageVector = InstUIIcons.notebookPen,
                                    contentDescription = stringResource(id = R.string.a11y_notebookOpenPageNotes),
                                    tint = colorResource(id = R.color.textLightest)
                                )
                            }
                        }
                        onAddBookmark?.let { BookmarkOverflowMenu(it) }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (uiState.screenState) {
                    is PageDetailsScreenState.Loading -> Loading(
                        color = Color(toolbarColor),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("loading")
                    )

                    is PageDetailsScreenState.Content -> {
                        // Local WebView-load tracking, kept in addition to the ViewModel's async
                        // highlights fetch: `onPageFinished` (real page load) and `highlights`
                        // (network round-trip) can complete in either order, and the highlight JS
                        // must only run once *both* have happened - see the LaunchedEffect below.
                        // A counter (not a boolean) so every real page load - including a native,
                        // content-unchanged reload triggered by the WebView's own dark/light
                        // "switch theme" button - is distinguishable from the previous one and
                        // re-triggers re-injection; a plain toggled-then-reset boolean would stay
                        // `true` across that kind of reload (it only ever flips to `false` when
                        // `uiState.htmlContent` itself changes), silently wiping the highlight spans
                        // that reload's fresh HTML load discards.
                        var webViewRef by remember { mutableStateOf<CanvasWebView?>(null) }
                        var pageLoadCount by remember { mutableStateOf(0) }

                        val combinedWebViewCallbacks = remember(webViewCallbacks) {
                            webViewCallbacks.copy(
                                onPageFinished = { view, url ->
                                    webViewCallbacks.onPageFinished(view, url)
                                    pageLoadCount++
                                }
                            )
                        }

                        val combinedApplyOnWebView: CanvasWebView.() -> Unit = {
                            webViewRef = this
                            applyOnWebView?.invoke(this)
                        }

                        val combinedApplyOnUpdate: CanvasWebViewWrapper.() -> Unit = {
                            applyOnUpdate?.invoke(this)
                            // Registered unconditionally (matching Horizon's proven
                            // ComposeNotesHighlightingCanvasWebView pattern) rather than gating on
                            // uiState.highlights.isNotEmpty() - gating it added a dependency on highlight
                            // fetch timing with no upside, since re-registering the interface under the
                            // same JS-side name is always safe.
                            webView.addTextSelectionInterface(
                                onTextSelect = { _, _, _, _, _, _, _ -> },
                                onHighlightedTextClick = { noteId, _, _, _, _, _, _, _, _, _, _ ->
                                    uiState.actions.onHighlightClicked(noteId)
                                },
                                onSelectionPositionChange = { _, _, _, _ -> }
                            )
                        }

                        LaunchedEffect(pageLoadCount, uiState.highlights) {
                            val view = webViewRef ?: return@LaunchedEffect
                            if (pageLoadCount > 0 && uiState.highlights.isNotEmpty()) {
                                view.evaluateTextSelectionInterface()
                                view.highlightRanges(uiState.highlights.mapNotNull { it.toWebViewHighlight() }) {}
                            }
                        }

                        ComposeCanvasWebViewWrapper(
                            content = uiState.htmlContent,
                            title = uiState.toolbarTitle,
                            baseUrl = uiState.baseUrl,
                            onLtiButtonPressed = { uiState.actions.onLtiButtonPressed(it) },
                            applyOnWebView = combinedApplyOnWebView,
                            applyOnUpdate = combinedApplyOnUpdate,
                            webViewCallbacks = combinedWebViewCallbacks,
                            embeddedWebViewCallbacks = embeddedWebViewCallbacks,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("pageDetailsWebView")
                        )
                    }
                }
            }
        }

        uiState.notebookTrayArgs?.let { trayArgs ->
            NotebookTray(
                args = trayArgs,
                courseColor = Color(toolbarColor),
                onDismiss = uiState.actions.onNotebookTrayDismiss
            )
        }

        if (studyToolsUiState.panelVisible) {
            StudyToolsPanel(
                uiState = studyToolsUiState,
                isTablet = booleanResource(id = R.bool.isDeviceTablet)
            )
        }
    }
}

@Composable
private fun BookmarkOverflowMenu(onAddBookmark: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OverflowMenu(
        modifier = Modifier.background(colorResource(id = R.color.backgroundLightestElevated)),
        showMenu = expanded,
        iconColor = colorResource(id = R.color.textLightest),
        onDismissRequest = { expanded = !expanded }
    ) {
        DropdownMenuItem(onClick = {
            expanded = false
            onAddBookmark()
        }) {
            Text(
                text = stringResource(id = R.string.addBookmark),
                color = colorResource(id = R.color.textDarkest)
            )
        }
    }
}
