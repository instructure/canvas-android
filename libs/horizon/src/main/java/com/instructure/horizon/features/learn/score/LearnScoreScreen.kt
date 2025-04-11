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
package com.instructure.horizon.features.learn.score

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.organisms.cards.CollapsableContentCard
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.stringValueWithoutTrailingZeros

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScoreScreen(courseId: Long, modifier: Modifier = Modifier, viewModel: LearnScoreViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadState(courseId)
    }

    LoadingStateWrapper(state.screenState) {
        LearnScoreContent(state, modifier)
    }
}

@Composable
private fun LearnScoreContent(state: LearnScoreUiState, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 24.dp)
    ) {
        item {
            CollapsableContentCard(
                title = "Average Score: ${state.grades?.currentScore.orDefault().stringValueWithoutTrailingZeros}%",
                expandableSubtitle = "Assignment Group Weights",
                expanded = isExpanded,
                onExpandChanged = { isExpanded = it },
                expandableContent = {
                    GroupWeightsContent(state.assignmentGroups)
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        assignmentsContent(state)
    }
}

private fun LazyListScope.assignmentsContent(state: LearnScoreUiState) {
    item {

    }
}

@Composable
private fun GroupWeightsContent(assignmentGroups: List<AssignmentGroup>) {
    Column {
        assignmentGroups.forEachIndexed { index, item ->
            GroupWeightItem(item)

            if (index != assignmentGroups.lastIndex) {
                HorizontalDivider(
                    color = HorizonColors.Surface.pagePrimary(),
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
private fun GroupWeightItem(assignmentGroup: AssignmentGroup) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        Column (
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = assignmentGroup.name.orEmpty(),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body()
            )

            HorizonSpace(SpaceSize.SPACE_8)

            Text(
                text = "${assignmentGroup.groupWeight.stringValueWithoutTrailingZeros}%",
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body()
            )
        }
    }
}