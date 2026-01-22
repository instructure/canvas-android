/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.todo

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasSwitch
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.features.dashboard.widget.todo.model.TodoItem
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.flow.SharedFlow
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun TodoWidget(
    refreshSignal: SharedFlow<Unit>,
    modifier: Modifier = Modifier
) {
    val viewModel: TodoWidgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            viewModel.refresh()
        }
    }

    TodoWidgetContent(
        modifier = modifier,
        uiState = uiState
    )
}

@Composable
fun TodoWidgetContent(
    modifier: Modifier = Modifier,
    uiState: TodoWidgetUiState
) {
    val density = LocalDensity.current
    var calendarCenterY by remember { mutableStateOf(0.dp) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.dailyToDoWidgetTitle),
            fontSize = 14.sp,
            color = colorResource(R.color.textDarkest),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.backgroundLightest)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Header with month and switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.monthTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(R.color.textDarkest)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.todoWidget_showCompleted),
                                fontSize = 14.sp,
                                color = Color(ThemePrefs.textButtonColor)
                            )

                            CanvasSwitch(
                                checked = uiState.showCompleted,
                                onCheckedChange = { uiState.onToggleShowCompleted() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calendar
                    uiState.calendarBodyUiState?.let { calendarBodyUiState ->
                        WeekCalendarView(
                            calendarBodyUiState = calendarBodyUiState,
                            selectedDay = uiState.selectedDay,
                            onDaySelected = uiState.onDaySelected,
                            onPageChanged = uiState.onPageChanged,
                            scrollToPageOffset = uiState.scrollToPageOffset,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    val positionInParent = coordinates.positionInParent()
                                    val height = coordinates.size.height
                                    with(density) {
                                        calendarCenterY = (positionInParent.y + height / 2).toDp()
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Divider
                    CanvasDivider(
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Todo items container
                    TodoItemsContainer(
                        todosLoading = uiState.todosLoading,
                        todosError = uiState.todosError,
                        todos = uiState.todos
                    )
                }
            }

            // Navigation buttons overlaying the card
            if (calendarCenterY > 0.dp) {
                uiState.calendarBodyUiState?.let {
                    CalendarNavigationButton(
                        iconRes = R.drawable.ic_chevron_left,
                        contentDescription = stringResource(R.string.a11y_calendarPreviousWeek),
                        onClick = { uiState.onNavigateWeek(-1) },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 8.dp, y = calendarCenterY - 12.dp)
                    )

                    CalendarNavigationButton(
                        iconRes = R.drawable.ic_chevron_right,
                        contentDescription = stringResource(R.string.a11y_calendarNextWeek),
                        onClick = { uiState.onNavigateWeek(1) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = calendarCenterY - 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarNavigationButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                color = Color(ThemePrefs.buttonColor),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(24.dp)
                .padding(0.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = Color(ThemePrefs.buttonTextColor),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun TodoItemsContainer(
    todosLoading: Boolean,
    todosError: Boolean,
    todos: List<TodoItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            todosLoading -> {
                TodoItemsLoading()
            }
            todosError -> {
                TodoItemsError()
            }
            todos.isEmpty() -> {
                TodoItemsEmpty()
            }
            else -> {
                TodoItemsList(todos = todos)
            }
        }
    }
}

@Composable
private fun TodoItemsLoading() {
    Loading(modifier = Modifier.height(100.dp))
}

@Composable
private fun TodoItemsError() {
    Text(
        text = stringResource(R.string.errorOccurred),
        fontSize = 14.sp,
        color = colorResource(R.color.textDark)
    )
}

@Composable
private fun TodoItemsEmpty() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: Add panda image and "Add To-do" button in future
        Text(
            text = stringResource(R.string.nothingUnreadSubtext),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.textDarkest)
        )
    }
}

@Composable
private fun TodoItemsList(todos: List<TodoItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: Implement todo items list
        Text(
            text = "Todo items (${todos.size})",
            fontSize = 14.sp,
            color = colorResource(R.color.textDark)
        )
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun TodoWidgetContentPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context
    AndroidThreeTen.init(context)

    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())

    TodoWidgetContent(
        uiState = TodoWidgetUiState(
            todosLoading = false,
            calendarBodyUiState = calendarStateMapper.createBodyUiState(
                expanded = false,
                selectedDay = LocalDate.now(),
                jumpToToday = false,
                scrollToPageOffset = 0,
                eventIndicators = emptyMap()
            ),
            monthTitle = LocalDate.now().month.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            ),
            todos = listOf(
                TodoItem(
                    id = 1,
                    title = "Complete Assignment 1",
                    courseName = "Introduction to Computer Science",
                    courseColor = "#FF0000",
                    dueDate = Date(),
                    points = 100.0
                ),
                TodoItem(
                    id = 2,
                    title = "Read Chapter 5",
                    courseName = "Advanced Mathematics",
                    courseColor = "#00FF00",
                    dueDate = Date(),
                    points = 50.0
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun TodoWidgetEmptyPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context
    AndroidThreeTen.init(context)

    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())

    TodoWidgetContent(
        uiState = TodoWidgetUiState(
            todosLoading = false,
            calendarBodyUiState = calendarStateMapper.createBodyUiState(
                expanded = false,
                selectedDay = LocalDate.now(),
                jumpToToday = false,
                scrollToPageOffset = 0,
                eventIndicators = emptyMap()
            ),
            monthTitle = LocalDate.now().month.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            ),
            todos = emptyList()
        )
    )
}