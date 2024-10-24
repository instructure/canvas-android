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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.parentapp.R
import com.instructure.parentapp.features.inbox.coursepicker.ParentInboxCoursePickerAction
import com.instructure.parentapp.features.inbox.coursepicker.ParentInboxCoursePickerUiState
import com.instructure.parentapp.features.inbox.coursepicker.StudentContextItem

@Composable
fun ParentInboxCoursePickerScreen(
    uiState: ParentInboxCoursePickerUiState,
    actionHandler: (ParentInboxCoursePickerAction) -> Unit
) {
    CanvasTheme {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.chooseACourseToMessage),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.textDark)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { actionHandler(ParentInboxCoursePickerAction.CloseDialog) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = stringResource(id = R.string.close),
                        tint = colorResource(id = R.color.textDarkest)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .heightIn(min = 0.dp, max = LocalConfiguration.current.screenHeightDp.dp / 2)
            ) {
                items(uiState.studentContextItems) { studentContextItem ->
                    StudentContextItemRow(
                        studentContextItem = studentContextItem,
                        onClick = {
                            actionHandler(
                                ParentInboxCoursePickerAction.StudentContextSelected(
                                    studentContextItem
                                )
                            )
                        }
                    )
                }
            }
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
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = studentContextItem.course.name,
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDarkest)
        )
        Text(
            text = stringResource(
                R.string.forStudentLabel,
                studentContextItem.user.shortName ?: studentContextItem.user.name
            ),
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark)
        )
    }
}

@Preview
@Composable
fun ParentInboxCoursePickerScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ParentInboxCoursePickerScreen(
        uiState = ParentInboxCoursePickerUiState(
            studentContextItems = listOf(
                StudentContextItem(
                    course = Course(name = "Course 1"),
                    user = User(name = "Student 1")
                ),
                StudentContextItem(
                    course = Course(name = "Course 2"),
                    user = User(name = "Student 2")
                )
            )
        ),
        actionHandler = {}
    )
}