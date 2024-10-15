/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.inbox.coursepicker.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.parentapp.R
import com.instructure.parentapp.features.inbox.coursepicker.ParentInboxCoursePickerAction
import com.instructure.parentapp.features.inbox.coursepicker.ParentInboxCoursePickerUiState
import com.instructure.parentapp.features.inbox.coursepicker.StudentContextItem

@Composable
fun ParentInboxCoursePickerScreen(
    uiState: ParentInboxCoursePickerUiState,
    actionHandler: (ParentInboxCoursePickerAction) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.chooseACourseToMessage),
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark)
        )

        Spacer(modifier = Modifier.height(8.dp))

        uiState.studentContextItems.forEach { studentContextItem ->
            StudentContextItemRow(
                studentContextItem = studentContextItem,
                onClick = {
                    actionHandler(ParentInboxCoursePickerAction.StudentContextSelected(studentContextItem))
                }
            )
        }
    }
}

@Composable
private fun StudentContextItemRow(
    studentContextItem: StudentContextItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = studentContextItem.course.name,
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDarkest)
        )
        Text(
            text = studentContextItem.user.name,
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark)
        )
    }
}