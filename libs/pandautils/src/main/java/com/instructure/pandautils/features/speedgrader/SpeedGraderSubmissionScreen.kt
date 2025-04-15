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
package com.instructure.pandautils.features.speedgrader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowWidthSizeClass
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.pandautils.compose.composables.DraggableResizableLayout
import com.instructure.pandautils.compose.composables.HorizontalDraggableResizableLayout

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SpeedGraderSubmissionScreen(
) {

    val horizontal = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
            currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM

    if (horizontal) {
        HorizontalDraggableResizableLayout(
            modifier = Modifier,
            leftContent = {
                GlideImage(
                    model = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Test-Logo.svg/783px-Test-Logo.svg.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            },
            rightContent = {
                Text(text = "Right Content")
            }
        )
    } else {
        DraggableResizableLayout(
            modifier = Modifier,
            topContent = {
                GlideImage(
                    model = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Test-Logo.svg/783px-Test-Logo.svg.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            },
            bottomContent = {
                Text(text = "Bottom Content")
            }
        )
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun SpeedGraderSubmissionScreenTabletPreview() {
    SpeedGraderSubmissionScreen()
}

@Preview
@Composable
fun SpeedGraderSubmissionScreenPhonePreview() {
    SpeedGraderSubmissionScreen()
}

@Preview(device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun SpeedGraderSubmissionScreenPhoneLandscapePreview() {
    SpeedGraderSubmissionScreen()
}