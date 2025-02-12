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

package com.instructure.parentapp.features.courses.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterialApi
import androidx.compose.material3.pullrefresh.PullRefreshDefaults
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.views.CanvasWebView


@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun CourseDetailsWebViewScreen(
    html: String,
    isRefreshing: Boolean,
    studentColor: Int,
    onRefresh: () -> Unit,
    applyOnWebView: (CanvasWebView) -> Unit,
    onLtiButtonPressed: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh,
        refreshThreshold = PullRefreshDefaults.RefreshingOffset
    )

    Box(
        modifier = Modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("CourseDetailsWebViewScreen")
    ) {
        ComposeCanvasWebViewWrapper(
            html = html,
            onLtiButtonPressed = onLtiButtonPressed,
            applyOnWebView = applyOnWebView
        )
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .testTag("pullRefreshIndicator"),
            contentColor = Color(studentColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailsWebViewScreenPreview() {
    CourseDetailsWebViewScreen(
        html = "WebView content",
        isRefreshing = false,
        studentColor = android.graphics.Color.BLACK,
        onRefresh = {},
        applyOnWebView = {},
        onLtiButtonPressed = {}
    )
}
