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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.calendar.CalendarBodyUiState
import com.instructure.pandautils.compose.composables.calendar.CalendarPageUiState
import com.instructure.pandautils.compose.composables.todo.ToDoItemType
import com.instructure.pandautils.compose.composables.todo.ToDoItemUiState
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TodoWidgetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        AndroidThreeTen.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun testWidgetShowsLoadingShimmer() {
        val uiState = TodoWidgetUiState(
            todosLoading = true,
            todos = emptyList()
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testWidgetShowsEmptyStateWhenNoTodos() {
        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = emptyList(),
            monthTitle = "February",
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("February").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsSingleTodo() {
        val todos = listOf(
            createToDoItemUiState(
                id = "1",
                title = "Complete Assignment 1",
                itemType = ToDoItemType.ASSIGNMENT
            )
        )

        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = todos,
            monthTitle = "February",
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Complete Assignment 1").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsMultipleTodos() {
        val todos = listOf(
            createToDoItemUiState(
                id = "1",
                title = "Complete Assignment 1",
                itemType = ToDoItemType.ASSIGNMENT
            ),
            createToDoItemUiState(
                id = "2",
                title = "Study for Quiz",
                itemType = ToDoItemType.QUIZ
            ),
            createToDoItemUiState(
                id = "3",
                title = "Read Chapter 5",
                itemType = ToDoItemType.PLANNER_NOTE
            )
        )

        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = todos,
            monthTitle = "February",
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Complete Assignment 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Study for Quiz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Chapter 5").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsMonthTitle() {
        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = emptyList(),
            monthTitle = "December",
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("December").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsCompletedToggle() {
        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = emptyList(),
            monthTitle = "February",
            showCompleted = false,
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Show Completed").assertIsDisplayed()
    }

    @Test
    fun testShowCompletedToggleChangesState() {
        var showCompleted by mutableStateOf(false)
        var toggleCalled = false

        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = emptyList(),
            monthTitle = "February",
            showCompleted = showCompleted,
            onToggleShowCompleted = {
                toggleCalled = true
                showCompleted = !showCompleted
            },
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(
                uiState = uiState.copy(showCompleted = showCompleted)
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ShowCompletedSwitch").performClick()

        assert(toggleCalled)
    }

    @Test
    fun testWidgetShowsErrorState() {
        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todosError = true,
            todos = emptyList(),
            monthTitle = "February",
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testWidgetShowsCheckedTodo() {
        val todos = listOf(
            createToDoItemUiState(
                id = "1",
                title = "Completed Assignment",
                itemType = ToDoItemType.ASSIGNMENT,
                isChecked = true
            )
        )

        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = todos,
            monthTitle = "February",
            showCompleted = true,
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Completed Assignment").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsDifferentTodoTypes() {
        val todos = listOf(
            createToDoItemUiState(
                id = "1",
                title = "Assignment",
                itemType = ToDoItemType.ASSIGNMENT
            ),
            createToDoItemUiState(
                id = "2",
                title = "Quiz",
                itemType = ToDoItemType.QUIZ
            ),
            createToDoItemUiState(
                id = "3",
                title = "Discussion",
                itemType = ToDoItemType.DISCUSSION
            ),
            createToDoItemUiState(
                id = "4",
                title = "Event",
                itemType = ToDoItemType.CALENDAR_EVENT
            )
        )

        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = todos,
            monthTitle = "February",
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Assignment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Discussion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsTodoWithContext() {
        val course = Course(id = 1, name = "Computer Science 101")
        val todos = listOf(
            createToDoItemUiState(
                id = "1",
                title = "Complete Assignment",
                itemType = ToDoItemType.ASSIGNMENT,
                canvasContext = course,
                contextLabel = "Computer Science 101"
            )
        )

        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = todos,
            monthTitle = "February",
            calendarBodyUiState = CalendarBodyUiState(
                previousPage = CalendarPageUiState(emptyList(), ""),
                currentPage = CalendarPageUiState(emptyList(), ""),
                nextPage = CalendarPageUiState(emptyList(), "")
            )
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Complete Assignment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Computer Science 101").assertIsDisplayed()
    }

    @Test
    fun testWidgetDisplaysDailyTitle() {
        val uiState = TodoWidgetUiState(
            todosLoading = false,
            todos = emptyList(),
            monthTitle = "February"
        )

        composeTestRule.setContent {
            TodoWidgetContent(uiState = uiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Daily To-do").assertIsDisplayed()
    }

    private fun createToDoItemUiState(
        id: String,
        title: String,
        itemType: ToDoItemType,
        isChecked: Boolean = false,
        canvasContext: CanvasContext = CanvasContext.emptyCourseContext(id = 1),
        contextLabel: String = ""
    ): ToDoItemUiState {
        return ToDoItemUiState(
            id = id,
            title = title,
            date = Date(),
            dateLabel = "Feb 15",
            contextLabel = contextLabel,
            canvasContext = canvasContext,
            itemType = itemType,
            isChecked = isChecked,
            iconRes = R.drawable.ic_assignment,
            tag = "",
            htmlUrl = "",
            isClickable = true,
            onSwipeToDone = {},
            onCheckboxToggle = {}
        )
    }
}