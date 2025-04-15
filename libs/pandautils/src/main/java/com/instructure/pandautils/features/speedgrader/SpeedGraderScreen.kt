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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasAppBar

@Composable
fun SpeedGraderScreen() {

    val pagerState = rememberPagerState(pageCount = { 3 })
    Scaffold(
        topBar = {
            CanvasAppBar(
                title = "Speed Grader",
                navigationActionClick = {},
                navIconRes = R.drawable.ic_back_arrow,)
        },
    ) { padding ->
        HorizontalPager(modifier = Modifier.padding(padding), state = pagerState) {
            SpeedGraderSubmissionScreen()
        }
    }
}