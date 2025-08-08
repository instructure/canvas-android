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
package com.instructure.pandautils.features.speedgrader.grade.comments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.CanvasDivider

@Composable
fun SpeedGraderCommentsScreen(
    expanded: Boolean,
    fixed: Boolean = false,
    onExpandToggle: () -> Unit
) {
    val viewModel: SpeedGraderCommentsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val haptic = LocalHapticFeedback.current
    val iconRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "expandedIconRotation")

    Column {
        CanvasDivider(modifier = Modifier.fillMaxWidth())
        val stateExpanded = stringResource(R.string.a11y_expanded)
        val stateCollapsed = stringResource(R.string.a11y_collapsed)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = if (expanded) stateExpanded else stateCollapsed
                }
                .clickable(
                    enabled = !uiState.isLoading
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onExpandToggle()
                }
                .padding(horizontal = 16.dp)
                .height(50.dp)

        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_discussion),
                contentDescription = null,
                tint = LocalCourseColor.current,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.speedGraderCommentsTitle, uiState.comments.size),
                color = colorResource(R.color.textDarkest),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = LocalCourseColor.current,
                    modifier = Modifier.size(24.dp)
                )
            } else if (!fixed) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    tint = colorResource(id = R.color.textDarkest),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(iconRotation)
                )
            }
        }
        CanvasDivider(modifier = Modifier.fillMaxWidth())
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
            label = "GroupExpandAnimation"
        ) {
            SpeedGraderCommentsSection(uiState, actionHandler = viewModel::handleAction)
        }
    }
}
