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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.features.pages.details.PageDetailsScreenState
import com.instructure.pandautils.features.pages.details.PageDetailsUiState
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.CanvasWebViewWrapper

@Composable
fun PageDetailsScreen(
    uiState: PageDetailsUiState,
    @ColorInt toolbarColor: Int,
    navigationActionClick: () -> Unit,
    webViewCallbacks: ComposeWebViewCallbacks,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks,
    modifier: Modifier = Modifier,
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

                    is PageDetailsScreenState.Content -> ComposeCanvasWebViewWrapper(
                        content = uiState.htmlContent,
                        title = uiState.toolbarTitle,
                        baseUrl = uiState.baseUrl,
                        onLtiButtonPressed = { uiState.actions.onLtiButtonPressed(it) },
                        applyOnWebView = applyOnWebView,
                        applyOnUpdate = applyOnUpdate,
                        webViewCallbacks = webViewCallbacks,
                        embeddedWebViewCallbacks = embeddedWebViewCallbacks,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("pageDetailsWebView")
                    )
                }
            }
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
