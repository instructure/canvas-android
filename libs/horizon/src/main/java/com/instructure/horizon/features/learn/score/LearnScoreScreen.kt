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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillSize
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.horizonui.organisms.cards.CollapsableContentCard
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.AssignmentStatus
import com.instructure.pandautils.utils.getStatus
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.stringValueWithoutTrailingZeros
import com.instructure.pandautils.utils.toFormattedString
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScoreScreen(courseId: Long, modifier: Modifier = Modifier, viewModel: LearnScoreViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadState(courseId)
    }

    LoadingStateWrapper(state.screenState) {
        LearnScoreContent(
            state,
            { viewModel.updateSelectedSortOption(it) },
            modifier
        )
    }
}

@Composable
private fun LearnScoreContent(
    state: LearnScoreUiState,
    onSelectedSortOptionChanged: (LearnScoreSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    LazyColumn(modifier = modifier) {
        item {
            CollapsableContentCard(
                title = stringResource(
                    R.string.averageScoreHeader,
                    state.grades?.currentScore.orDefault().stringValueWithoutTrailingZeros
                ),
                expandableSubtitle = stringResource(R.string.assignmentGroupWeights),
                expanded = isExpanded,
                onExpandChanged = { isExpanded = it },
                expandableContent = {
                    GroupWeightsContent(state.assignmentGroups)
                },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            AssignmentsContent(state, onSelectedSortOptionChanged = {
                onSelectedSortOptionChanged(it)
            })
        }
    }
}

@Composable
private fun AssignmentsContent(
    state: LearnScoreUiState,
    onSelectedSortOptionChanged: (LearnScoreSortOption) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(HorizonCornerRadius.level2)
            .background(HorizonColors.Surface.cardPrimary())
    ) {
        var isSelectOpen by remember { mutableStateOf(false) }
        var isSelectFocused by remember { mutableStateOf(false) }
        val options = LearnScoreSortOption.entries.associateBy { stringResource(it.label) }
        Column (
            modifier = Modifier
                .padding(vertical = 16.dp)
        ) {
            SingleSelect(
                state = SingleSelectState(
                    label = stringResource(R.string.sortBy),
                    isFocused = isSelectFocused,
                    isMenuOpen = isSelectOpen,
                    size = SingleSelectInputSize.Medium,
                    options = options.keys.toList(),
                    selectedOption = stringResource(state.selectedSortOption.label),
                    onOptionSelected = { selected ->
                        options[selected]?.let {
                            onSelectedSortOptionChanged(it)
                        }
                    },
                    onFocusChanged = { isSelectFocused = it },
                    onMenuOpenChanged = { isSelectOpen = it },
                ),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 24.dp)
            )

            AnimatedContent(
                state.sortedAssignments,
                label = "AssignmentsContentAnimation",
            ) { assignments ->
                Column {
                    assignments.forEach { assignment ->
                        Column {
                            AssignmentItem(assignment)

                            if (assignment != state.sortedAssignments.last()) {
                                HorizontalDivider(
                                    color = HorizonColors.Surface.pagePrimary(),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentItem(assignment: Assignment) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.scoresItemassignmentName, assignment.name.orEmpty()),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body()
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            text = stringResource(
                R.string.scoresItemDueDate,
                assignment.dueDate?.toFormattedString(DateFormat.SHORT) ?: stringResource(R.string.noDueDate)
            ),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body()
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.scoresItemStatus),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body()
            )

            HorizonSpace(SpaceSize.SPACE_4)

            AssignmentStatusPill(assignment)
        }

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            text = stringResource(
                R.string.scoresItemResult,
                assignment.lastActualSubmission?.score?.stringValueWithoutTrailingZeros ?: "-",
                assignment.pointsPossible.stringValueWithoutTrailingZeros
            ),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body()
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Feedback:",
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body()
            )

            if (assignment.lastActualSubmission?.submissionComments?.isNotEmpty().orDefault()) {
                Icon(
                    painter = painterResource(R.drawable.mark_unread_chat_alt),
                    contentDescription = null,
                    tint = HorizonColors.Icon.default(),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = (assignment.lastActualSubmission?.submissionComments?.size ?: 0).toString(),
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body()
                )
            } else {
                Text(
                    text = stringResource(R.string.noFeedback),
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body()
                )
            }
        }
    }
}

@Composable
private fun AssignmentStatusPill(assignment: Assignment) {
    val status = assignment.getStatus()
    val pillType = if (status == AssignmentStatus.Late || status == AssignmentStatus.Missing) {
        PillType.DANGER
    } else {
        PillType.INSTITUTION
    }
    Pill(
        label = stringResource(status.label),
        style = PillStyle.OUTLINE,
        case = PillCase.UPPERCASE,
        type = pillType,
        size = PillSize.SMALL
    )
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
                text = stringResource(
                    R.string.percentageValue,
                    assignmentGroup.groupWeight.stringValueWithoutTrailingZeros
                ),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body()
            )
        }
    }
}