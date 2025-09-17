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
package com.instructure.pandautils.features.assignments.details.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandares.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.features.reminder.ReminderViewState
import com.instructure.pandautils.features.reminder.composables.ReminderView
import com.instructure.pandautils.utils.toFormattedString

@Composable
fun DueDateReminderLayout(
    reminderViewStates: List<ReminderViewState>,
    onAddClick: (String?) -> Unit,
    onRemoveClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        for (reminderViewState in reminderViewStates) {
            DueDateBlock(reminderViewState)
            ReminderView(
                viewState = reminderViewState,
                onAddClick = onAddClick,
                onRemoveClick = onRemoveClick
            )
            CanvasDivider()
        }
    }
}

@Composable
private fun DueDateBlock(
    reminderViewState: ReminderViewState
) {
    Text(
        modifier = Modifier.padding(top = 24.dp),
        text = reminderViewState.dueLabel ?: stringResource(id = R.string.dueLabel),
        color = colorResource(id = R.color.textDark),
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        modifier = Modifier.padding(bottom = 14.dp),
        text = "${reminderViewState.dueDate?.toFormattedString()}",
        color = colorResource(id = R.color.textDarkest),
        fontSize = 16.sp
    )
}