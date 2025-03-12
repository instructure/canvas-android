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
package com.instructure.pandautils.features.assignments.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.GroupedListView
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.getSubmissionStateLabel
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toFormattedString
import java.util.Date

@Composable
fun AssignmentListScreen(
    state: AssignmentListUiState,
    contextColor: Color
) {
    AssignmentListContentView(state, contextColor)
}

@Composable
private fun AssignmentListContentView(
    state: AssignmentListUiState,
    contextColor: Color
) {
    GroupedListView(
        state = state.listState,
        itemView = { AssignmentListItemView(it, contextColor) },
    ) { }
}

@Composable
private fun AssignmentListItemView(item: AssignmentGroupItemState, contextColor: Color) {
    val assignment = item.assignment
    Row(
        modifier = Modifier
            .background(colorResource(R.color.backgroundLightest))
            .padding(vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Icon(
            painter = painterResource(assignment.getAssignmentIcon()),
            tint = contextColor,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                assignment.name.orEmpty(),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                if (item.showSubmissionDetails) {
                    Text(
                        assignment.dueDate?.toFormattedString() ?: stringResource(R.string.noDueDate),
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                    val submissionStateLabel = assignment.getSubmissionStateLabel()
                    if (submissionStateLabel != SubmissionStateLabel.NONE) {
                        AssignmentDivider()
                        Icon(
                            painter = painterResource(submissionStateLabel.iconRes),
                            tint = colorResource(submissionStateLabel.colorRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                        )
                        Text(
                            stringResource(submissionStateLabel.labelRes),
                            color = colorResource(submissionStateLabel.colorRes),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
                if (item.showAssignmentDetails){
                    if (assignment.lockDate?.before(Date()).orDefault()) {
                        Text(
                            stringResource(R.string.closed),
                            color = colorResource(id = R.color.textDark),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                        )
                        AssignmentDivider()
                    }
                    Text(
                        assignment.dueDate?.toFormattedString() ?: stringResource(R.string.noDueDate),
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                if (item.showSubmissionDetails) {
                    Text(
                        stringResource(
                            R.string.assignmentPointsPerMaximum,
                            if (assignment.isGraded()) assignment.submission?.score?.toFormattedString() ?: "-" else "-",
                            assignment.pointsPossible.toFormattedString()
                        ),
                        color = contextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (item.showAssignmentDetails) {
                    if (assignment.needsGradingCount.toInt() != 0) {
                        AssignmentNeedsGradingChip(
                            assignment.needsGradingCount.toInt(),
                            contextColor
                        )
                        AssignmentDivider()
                    }
                    Text(
                        "${assignment.pointsPossible.toFormattedString()} points",
                        color = contextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentNeedsGradingChip(count: Int, contextColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(contextColor)
    ) {
        Text(
            stringResource(R.string.needsGrading, count),
            modifier = Modifier
                .padding(4.dp),
            color = colorResource(R.color.textLightest),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun AssignmentDivider() {
    Row {
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            color = colorResource(id = R.color.textDark),
            thickness = 1.dp,
            modifier = Modifier
                .height(16.dp)
                .width(1.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}