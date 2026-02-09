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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasSwitch
import com.instructure.pandautils.compose.composables.ShimmerBox
import com.instructure.pandautils.compose.composables.calendar.CalendarColors
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.compose.composables.todo.ToDoItem
import com.instructure.pandautils.compose.composables.todo.ToDoItemType
import com.instructure.pandautils.compose.composables.todo.ToDoItemUiState
import com.instructure.pandautils.features.todolist.OnToDoCountChanged
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.getFragmentActivityOrNull
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
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: TodoWidgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.updateToDoCount) {
        val activity = context.getActivityOrNull() as? OnToDoCountChanged
        activity?.refreshToDoCount()
        uiState.onToDoCountUpdated()
    }

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            viewModel.refresh()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            onShowSnackbar(message, null, null)
            uiState.onSnackbarDismissed()
        }
    }

    LaunchedEffect(uiState.confirmationSnackbarData) {
        uiState.confirmationSnackbarData?.let { snackbarData ->
            val messageRes = if (snackbarData.markedAsDone) {
                R.string.todoMarkedAsDone
            } else {
                R.string.todoMarkedAsNotDone
            }
            val message = context.getString(messageRes, snackbarData.title)
            onShowSnackbar(message, context.getString(R.string.todoMarkedAsDoneSnackbarUndo), {
                uiState.onUndoMarkAsDoneUndone(snackbarData.itemId, snackbarData.markedAsDone)
            })
            uiState.onMarkedAsDoneSnackbarDismissed()
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
                                color = Color(uiState.color.color())
                            )

                            CanvasSwitch(
                                checked = uiState.showCompleted,
                                onCheckedChange = { uiState.onToggleShowCompleted() },
                                color = Color(uiState.color.color()),
                                modifier = Modifier.testTag("ShowCompletedSwitch")
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
                            calendarColors = CalendarColors(
                                selectedDayIndicatorColor = Color(uiState.color.color()),
                                selectedDayTextColor = colorResource(R.color.textLightest),
                                todayTextColor = Color(uiState.color.color())
                            ),
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
                        todos = uiState.todos.filter { it.id !in uiState.removingItemIds },
                        onTodoClick = uiState.onTodoClick,
                        onRefresh = uiState.onRefresh,
                        onAddTodoClick = uiState.onAddTodoClick,
                        buttonColor = Color(uiState.color.color())
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
                        buttonColor = Color(uiState.color.color()),
                        iconColor = colorResource(R.color.textLightest),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 8.dp, y = calendarCenterY - 12.dp)
                    )

                    CalendarNavigationButton(
                        iconRes = R.drawable.ic_chevron_right,
                        contentDescription = stringResource(R.string.a11y_calendarNextWeek),
                        onClick = { uiState.onNavigateWeek(1) },
                        buttonColor = Color(uiState.color.color()),
                        iconColor = colorResource(R.color.textLightest),
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
    buttonColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                color = buttonColor,
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
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TodoItemsContainer(
    todosLoading: Boolean,
    todosError: Boolean,
    todos: List<ToDoItemUiState>,
    onTodoClick: (FragmentActivity, String) -> Unit,
    onRefresh: () -> Unit,
    onAddTodoClick: (FragmentActivity) -> Unit,
    buttonColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 300)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            todosLoading -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    TodoItemsLoading()
                }
            }

            todosError -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    TodoItemsError(
                        onRefresh = onRefresh,
                        buttonColor = buttonColor
                    )
                }
            }

            todos.isEmpty() -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    TodoItemsEmpty(
                        onAddTodoClick = onAddTodoClick,
                        buttonColor = buttonColor
                    )
                }
            }

            else -> {
                TodoItemsList(
                    todos = todos,
                    onTodoClick = onTodoClick,
                    checkboxColor = buttonColor
                )
            }
        }
    }
}

@Composable
private fun TodoItemsLoading() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        repeat(3) { index ->
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(4.dp)
            )
            if (index < 2) {
                CanvasDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun TodoItemsError(
    onRefresh: () -> Unit = {},
    buttonColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 24.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_panda_notsupported),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(width = 102.dp, height = 104.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.todoWidget_errorTitle),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.textDarkest),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.todoWidget_errorMessage),
                fontSize = 14.sp,
                color = colorResource(R.color.textDark),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            shape = RoundedCornerShape(100.dp),
            modifier = Modifier.height(30.dp),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 0.dp,
                end = 8.dp,
                bottom = 0.dp
            )
        ) {
            Text(
                text = stringResource(R.string.todoWidget_refresh),
                color = colorResource(R.color.textLightest),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Icon(
                painter = painterResource(R.drawable.ic_refresh_lined),
                contentDescription = null,
                tint = colorResource(R.color.textLightest),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun TodoItemsEmpty(
    onAddTodoClick: (FragmentActivity) -> Unit = {},
    buttonColor: Color
) {
    val activity = LocalContext.current.getFragmentActivityOrNull()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 24.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_no_events),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(width = 160.dp, height = 106.dp)
        )

        Text(
            text = stringResource(R.string.todoWidget_emptyTitle),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colorResource(R.color.textDarkest),
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.todoWidget_emptyMessage),
            fontSize = 14.sp,
            color = colorResource(R.color.textDark),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { activity?.let { onAddTodoClick(it) } },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(30.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                0.dp,
                end = 12.dp,
                bottom = 0.dp,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_lined),
                contentDescription = null,
                tint = colorResource(R.color.textLightest),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = stringResource(R.string.todoWidget_addTodo),
                color = colorResource(R.color.textLightest),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun TodoItemsList(
    todos: List<ToDoItemUiState>,
    onTodoClick: (FragmentActivity, String) -> Unit,
    checkboxColor: Color,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.getFragmentActivityOrNull()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        todos.forEach { todo ->
            key(todo.id) {
                ToDoItem(
                    item = todo,
                    onCheckedChange = {
                        todo.onCheckboxToggle(!todo.isChecked)
                    },
                    onClick = {
                        activity?.let { fragmentActivity ->
                            todo.htmlUrl?.let { htmlUrl ->
                                onTodoClick(fragmentActivity, htmlUrl)
                            }
                        }
                    },
                    checkboxColor = checkboxColor
                )
                if (todo != todos.last()) {
                    CanvasDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
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
                ToDoItemUiState(
                    id = "1",
                    title = "Complete Assignment 1",
                    date = Date(),
                    dateLabel = "Due Today",
                    contextLabel = "Introduction to Computer Science",
                    canvasContext = CanvasContext.emptyCourseContext(1),
                    itemType = ToDoItemType.ASSIGNMENT,
                    isChecked = false,
                    iconRes = R.drawable.ic_assignment,
                    tag = "100 pts"
                ),
                ToDoItemUiState(
                    id = "2",
                    title = "Read Chapter 5",
                    date = Date(),
                    dateLabel = "Due Tomorrow",
                    contextLabel = "Advanced Mathematics",
                    canvasContext = CanvasContext.emptyCourseContext(2),
                    itemType = ToDoItemType.ASSIGNMENT,
                    isChecked = false,
                    iconRes = R.drawable.ic_assignment,
                    tag = "50 pts"
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

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun TodoWidgetLoadingPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context
    AndroidThreeTen.init(context)

    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())

    TodoWidgetContent(
        uiState = TodoWidgetUiState(
            todosLoading = true,
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

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun TodoWidgetErrorPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context
    AndroidThreeTen.init(context)

    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())

    TodoWidgetContent(
        uiState = TodoWidgetUiState(
            todosLoading = false,
            todosError = true,
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