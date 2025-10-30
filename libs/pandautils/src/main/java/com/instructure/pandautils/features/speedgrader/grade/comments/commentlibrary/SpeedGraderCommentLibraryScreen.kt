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

package com.instructure.pandautils.features.speedgrader.grade.comments.commentlibrary

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.speedgrader.grade.comments.composables.SpeedGraderCommentInput


internal const val COMMENT_LIBRARY_INITIAL_COMMENT_VALUE_ROUTE_PARAM = "initialCommentValue"

@Composable
fun SpeedGraderCommentLibraryScreen(
    onDismissRequest: (String) -> Unit,
    initialCommentValue: String,
    onSendCommentClicked: (String) -> Unit
) {
    NavHost(
        navController = rememberNavController(),
        startDestination = "commentLibrary/${Uri.encode(initialCommentValue)}"
    ) {
        dialog(
            route = "commentLibrary/{$COMMENT_LIBRARY_INITIAL_COMMENT_VALUE_ROUTE_PARAM}",
            arguments = listOf(
                navArgument(COMMENT_LIBRARY_INITIAL_COMMENT_VALUE_ROUTE_PARAM) {
                    type = NavType.StringType
                }
            ),
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            val viewModel: SpeedGraderCommentLibraryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            FullScreenDialog(
                onDismissRequest = {
                    onDismissRequest(uiState.commentValue)
                }
            ) {
                SpeedGraderCommentLibraryScreen(
                    uiState = uiState,
                    onDismissRequest = onDismissRequest,
                    onSendCommentClicked = onSendCommentClicked
                )
            }
        }
    }
}

@Composable
private fun SpeedGraderCommentLibraryScreen(
    uiState: SpeedGraderCommentLibraryUiState,
    onDismissRequest: (String) -> Unit,
    onSendCommentClicked: (String) -> Unit
) {
    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.toolbarCommentLibrary),
                navigationActionClick = {
                    onDismissRequest(uiState.commentValue)
                },
                navIconRes = R.drawable.ic_arrow_down,
                navIconContentDescription = stringResource(id = R.string.close),
                backgroundColor = LocalCourseColor.current,
                contentColor = colorResource(id = R.color.textLightest)
            )
        }
    ) {
        SpeedGraderCommentLibraryContent(
            uiState = uiState,
            onDismissRequest = onDismissRequest,
            onSendCommentClicked = onSendCommentClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        )
    }
}

@Composable
private fun SpeedGraderCommentLibraryContent(
    uiState: SpeedGraderCommentLibraryUiState,
    onDismissRequest: (String) -> Unit,
    onSendCommentClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        if (uiState.isLoading) {
            Loading(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("commentLibraryListColumn"),
            ) {
                itemsIndexed(uiState.items) { index, item ->
                    Text(
                        text = item,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.textDarkest),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                uiState.onCommentValueChanged(item)
                            }
                            .padding(vertical = 14.dp)
                            .testTag("commentLibraryItem")
                    )
                    if (index != uiState.items.lastIndex) {
                        CanvasDivider()
                    }
                }
            }
        }
        SpeedGraderCommentInput(
            commentText = uiState.commentValue,
            onCommentFieldChanged = uiState.onCommentValueChanged,
            onCommentLibraryClicked = {
                onDismissRequest(uiState.commentValue)
            },
            sendCommentClicked = {
                onSendCommentClicked(uiState.commentValue)
            },
            isOnCommentLibrary = true,
            testTag = "commentLibraryFilterInputField"
        )
    }
}

@Preview
@Composable
private fun CommentLibraryScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    SpeedGraderCommentLibraryScreen(
        uiState = SpeedGraderCommentLibraryUiState(
            onCommentValueChanged = {},
            isLoading = true
        ),
        onDismissRequest = {},
        onSendCommentClicked = {}
    )
}

@Preview
@Composable
private fun CommentLibraryScreenPreview() {
    SpeedGraderCommentLibraryScreen(
        uiState = SpeedGraderCommentLibraryUiState(
            onCommentValueChanged = {},
            isLoading = false,
            items = listOf(
                "You've shown a solid grasp of the core concepts here. Your explanation of [specific concept] was particularly clear and insightful. To further strengthen your work, consider exploring [related idea or application] in more detail.",
                "This work demonstrates promising ideas, especially in [specific aspect you liked]. To take it to the next level, focus on developing [area needing more depth] with more specific examples and evidence. Think about how [relevant question] might further inform your analysis.",
            )
        ),
        onDismissRequest = {},
        onSendCommentClicked = {}
    )
}
