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

package com.instructure.parentapp.features.managestudents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.utils.ThemePrefs


@Composable
internal fun ManageStudentsScreen(
    uiState: ManageStudentsUiState,
    actionHandler: (ManageStudentsAction) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = stringResource(id = R.string.screenTitleManageStudents),
                    navigationActionClick = {
                        navigationActionClick()
                    }
                )
            },
            content = { padding ->
                if (uiState.isLoadError) {
                    ErrorContent(
                        errorMessage = stringResource(id = R.string.errorLoadingStudents),
                        retryClick = {
                            actionHandler(ManageStudentsAction.Refresh)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.studentListItems.isEmpty() && !uiState.isLoading) {
                    EmptyContent(
                        emptyMessage = stringResource(id = R.string.noStudentsErrorDescription),
                        imageRes = R.drawable.panda_manage_students,
                        buttonText = stringResource(id = R.string.noStudentsRefresh),
                        buttonClick = {
                            actionHandler(ManageStudentsAction.Refresh)
                        },
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    )
                } else {
                    StudentListContent(
                        uiState = uiState,
                        actionHandler = actionHandler,
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.testTag("addStudentButton"),
                    backgroundColor = Color(ThemePrefs.buttonColor),
                    onClick = {
                        actionHandler(ManageStudentsAction.AddStudent)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        tint = Color(ThemePrefs.buttonTextColor),
                        contentDescription = stringResource(id = R.string.addNewStudent)
                    )
                }
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StudentListContent(
    uiState: ManageStudentsUiState,
    actionHandler: (ManageStudentsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = {
            actionHandler(ManageStudentsAction.Refresh)
        }
    )

    val dialogUiState = uiState.colorPickerDialogUiState
    if (dialogUiState.showColorPickerDialog) {
        StudentColorPickerDialog(
            initialUserColor = dialogUiState.initialUserColor,
            userColors = dialogUiState.userColors,
            saving = dialogUiState.isSavingColor,
            error = dialogUiState.isSavingColorError,
            onDismiss = {
                actionHandler(ManageStudentsAction.HideColorPickerDialog)
            },
            onColorSelected = {
                actionHandler(ManageStudentsAction.StudentColorChanged(dialogUiState.studentId, it))
            }
        )
    }

    Box(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.studentListItems) {
                StudentListItem(it, actionHandler)
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.isLoading,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .testTag("pullRefreshIndicator"),
            contentColor = Color(ThemePrefs.primaryColor)
        )
    }
}

@Composable
private fun StudentListItem(
    uiState: StudentItemUiState,
    actionHandler: (ManageStudentsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                actionHandler(ManageStudentsAction.StudentTapped(uiState.studentId))
            }
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
            .testTag("studentListItem"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            imageUrl = uiState.avatarUrl,
            name = uiState.studentName,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = buildAnnotatedString {
                append(uiState.studentName)
                if (!uiState.studentPronouns.isNullOrEmpty()) {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(" (${uiState.studentPronouns})")
                    }
                }
            },
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        val changeColorContentDescription = stringResource(id = R.string.changeStudentColorLabel, uiState.studentName)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .testTag("studentColor")
                .semantics(mergeDescendants = true) {
                    contentDescription = changeColorContentDescription
                    role = Role.Button
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color(uiState.studentColor.color()))
                ) {
                    actionHandler(ManageStudentsAction.ShowColorPickerDialog(uiState.studentId, uiState.studentColor))
                }
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color = Color(uiState.studentColor.color()))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageStudentsPreview() {
    ContextKeeper.appContext = LocalContext.current
    ManageStudentsScreen(
        uiState = ManageStudentsUiState(
            isLoading = false,
            studentListItems = listOf(
                StudentItemUiState(studentId = 1, studentName = "Student 1", studentPronouns = "They/Them"),
                StudentItemUiState(studentId = 2, studentName = "Student 2"),
                StudentItemUiState(studentId = 3, studentName = "Student 3"),
            )
        ),
        actionHandler = {},
        navigationActionClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ManageStudentsEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    ManageStudentsScreen(
        uiState = ManageStudentsUiState(
            isLoading = false,
            studentListItems = emptyList()
        ),
        actionHandler = {},
        navigationActionClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ManageStudentsErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ManageStudentsScreen(
        uiState = ManageStudentsUiState(
            isLoadError = true
        ),
        actionHandler = {},
        navigationActionClick = {}
    )
}