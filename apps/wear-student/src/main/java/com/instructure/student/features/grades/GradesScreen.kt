/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.student.features.grades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.rememberScalingLazyListState
import com.instructure.candroid.R
import com.instructure.student.Empty
import com.instructure.student.Loading

data class Grade(
    val title: String
)

@Composable
fun GradesScreen(viewModel: GradesViewModel = viewModel()) {
    val uiState = viewModel.gradesState.collectAsState()
    when (uiState.value) {
        is GradesUiState.Loading -> Loading()
        is GradesUiState.Empty -> Empty()
        is GradesUiState.Success -> GradeList((uiState.value as GradesUiState.Success).grades)
    }
}

@Composable
fun GradeList(grades: List<GradeItem>) {
    val listState = rememberScalingLazyListState()
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = AutoCenteringParams(itemIndex = 1),
        state = listState
    ) {
        item {
            Text(text = "Grades", modifier = Modifier.fillMaxWidth(), color = colorResource(id = R.color.textLightest))
        }
        grades.forEach { grade ->
            item {
                GradeItem(grade)
            }
        }
    }
}

@Composable
fun GradeItem(gradeItem: GradeItem) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(gradeItem.color)),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.backgroundDark))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            ) {
                Text(
                    text = gradeItem.name,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .weight(2f),
                    fontSize = MaterialTheme.typography.caption2.fontSize,
                    maxLines = 2,
                    lineHeight = MaterialTheme.typography.caption2.fontSize * 1.1f,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = R.color.textLightest),
                )
                Text(
                    text = gradeItem.grade.ifBlank { "N/A" },
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    color = Color(gradeItem.color),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    fontSize = MaterialTheme.typography.caption2.fontSize,
                )
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    GradesScreen()
}