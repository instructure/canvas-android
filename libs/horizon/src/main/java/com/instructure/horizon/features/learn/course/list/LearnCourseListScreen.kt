/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.course.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.CollapsableHeaderScreen

@Composable
fun LearnCourseListScreen() {
    CollapsableHeaderScreen(
        headerContent = {
            Text(
                text = "Dummy course list screen",
                style = HorizonTypography.h1
            )
        },
        bodyContent = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                (1..20).forEach {
                    Card(Modifier.padding(16.dp)) {
                        Text(
                            text = "Dummy course $it",
                            style = HorizonTypography.p1
                        )
                    }
                }
            }
        }
    )
}