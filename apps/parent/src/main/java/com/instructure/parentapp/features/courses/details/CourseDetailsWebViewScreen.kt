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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.views.CanvasWebView


@Composable
internal fun CourseDetailsWebViewScreen(
    html: String,
    applyOnWebView: (CanvasWebView) -> Unit,
    onLtiButtonPressed: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("CourseDetailsWebViewScreen")
    ) {
        ComposeCanvasWebViewWrapper(
            html = html,
            onLtiButtonPressed = onLtiButtonPressed,
            applyOnWebView = applyOnWebView
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailsWebViewScreenPreview() {
    CourseDetailsWebViewScreen(
        html = "Html content",
        applyOnWebView = {},
        onLtiButtonPressed = {}
    )
}
