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
package com.instructure.student.widget.grades.courseselector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.models.Course
import com.instructure.pandares.R

@Composable
fun CourseSelectorContentScreen(
    uiState: CourseSelectorUiState,
    paddingValues: PaddingValues,
    onCourseSelected: (Course) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.backgroundLightest))
            .padding(paddingValues)
    ) {
        items(uiState.courses) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCourseSelected(it)
                    }
                    .padding(16.dp),
                text = it.name,
                style = MaterialTheme.typography.body1.copy(
                    color = colorResource(R.color.textDarkest),
                )
            )
            HorizontalDivider(thickness = 1.dp)
        }
    }
}
