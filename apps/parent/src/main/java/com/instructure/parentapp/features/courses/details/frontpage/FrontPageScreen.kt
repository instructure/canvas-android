/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.courses.details.frontpage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.features.courses.details.CourseDetailsWebViewScreen


@Composable
internal fun FrontPageScreen(
    applyOnWebView: (CanvasWebView) -> Unit,
    onLtiButtonPressed: (String) -> Unit,
    showSnackbar: (String) -> Unit
) {
    val viewModel: FrontPageViewModel = viewModel()
    val uiState by remember { viewModel.uiState }.collectAsState()
    val events = viewModel.events
    LaunchedEffect(events) {
        events.collect { action ->
            when (action) {
                is FrontPageViewModelAction.ShowSnackbar -> {
                    showSnackbar(action.message)
                }
            }
        }
    }

    FrontPageContent(uiState, viewModel::handleAction, applyOnWebView, onLtiButtonPressed)
}

@Composable
internal fun FrontPageContent(
    uiState: FrontPageUiState,
    actionHandler: (FrontPageAction) -> Unit,
    applyOnWebView: (CanvasWebView) -> Unit,
    onLtiButtonPressed: (String) -> Unit
) {
    when {
        uiState.isLoading -> {
            Loading(
                color = Color(uiState.studentColor),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("loading")
            )
        }

        uiState.isError -> {
            ErrorContent(
                errorMessage = stringResource(id = R.string.generalUnexpectedError),
                retryClick = {
                    actionHandler(FrontPageAction.Refresh)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> {
            CourseDetailsWebViewScreen(
                html = uiState.htmlContent,
                isRefreshing = uiState.isRefreshing,
                studentColor = uiState.studentColor,
                onRefresh = { actionHandler(FrontPageAction.Refresh) },
                applyOnWebView = applyOnWebView,
                onLtiButtonPressed = onLtiButtonPressed,
                baseUrl = uiState.baseUrl
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FrontPageScreenPreview(
    @PreviewParameter(FrontPageScreenPreviewParameterProvider::class) uiState: FrontPageUiState
) {
    FrontPageContent(
        uiState = uiState,
        actionHandler = {},
        applyOnWebView = {},
        onLtiButtonPressed = {}
    )
}

private class FrontPageScreenPreviewParameterProvider : PreviewParameterProvider<FrontPageUiState> {
    override val values: Sequence<FrontPageUiState>
        get() = sequenceOf(
            FrontPageUiState(
                studentColor = android.graphics.Color.RED,
                isLoading = true,
                isError = false
            ),
            FrontPageUiState(
                studentColor = android.graphics.Color.BLUE,
                isLoading = false,
                isError = false,
                htmlContent = "Front Page Content"
            ),
            FrontPageUiState(
                studentColor = android.graphics.Color.GREEN,
                isLoading = false,
                isError = true
            )
        )
}
