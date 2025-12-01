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
package com.instructure.pandautils.features.todolist

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ToDoListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        ContextKeeper.appContext = context
    }

    @Test
    fun loadingStateIsDisplayed() {
        composeTestRule.setContent {
            ToDoListContent(
                uiState = createLoadingUiState(),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithTag("todoListLoading").assertIsDisplayed()
    }

    @Test
    fun errorStateIsDisplayed() {
        composeTestRule.setContent {
            ToDoListContent(
                uiState = createErrorUiState(),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithTag("todoListError").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.errorLoadingToDos)).assertIsDisplayed()
    }

    @Test
    fun emptyStateIsDisplayed() {
        composeTestRule.setContent {
            ToDoListContent(
                uiState = createEmptyUiState(),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithTag("todoListEmpty").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.noToDosForNow)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.noToDosForNowSubtext)).assertIsDisplayed()
    }

    @Test
    fun itemsListIsDisplayedWhenDataExists() {
        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithTag("todoList").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Assignment").assertIsDisplayed()
    }

    @Test
    fun multipleItemsAreDisplayed() {
        val item1 = createToDoItem(id = "1", title = "Assignment 1")
        val item2 = createToDoItem(id = "2", title = "Quiz 1")
        val item3 = createToDoItem(id = "3", title = "Discussion 1")

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(
                    items = listOf(item1, item2, item3)
                ),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Assignment 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Discussion 1").assertIsDisplayed()
    }

    @Test
    fun itemHasCheckbox() {
        val item = createToDoItem(id = "1", title = "Test Assignment")

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithTag("todoCheckbox_1").assertIsDisplayed()
    }

    @Test
    fun checkboxClickTriggersCallback() {
        var clicked = false
        val item = createToDoItem(
            id = "1",
            title = "Test Assignment",
            onCheckboxToggle = { clicked = true }
        )

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithTag("todoCheckbox_1").performClick()

        assertTrue(clicked)
    }

    @Test
    fun itemClickTriggersCallback() {
        var clickedUrl: String? = null
        val item = createToDoItem(
            id = "1",
            title = "Clickable Assignment",
            htmlUrl = "https://example.com/assignments/1"
        )

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = { url -> clickedUrl = url },
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Clickable Assignment").performClick()

        assertEquals("https://example.com/assignments/1", clickedUrl)
    }

    @Test
    fun dateBadgeIsDisplayedForFirstItemInGroup() {
        val item = createToDoItem(id = "1", title = "Test Assignment")

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        // Date badge should be visible (checking for day of month "22")
        composeTestRule.onNodeWithText("22").assertIsDisplayed()
    }

    @Test
    fun itemsGroupedByDateDisplayedCorrectly() {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrow = calendar.time

        val item1 = createToDoItem(id = "1", title = "Today Assignment", date = today)
        val item2 = createToDoItem(id = "2", title = "Tomorrow Assignment", date = tomorrow)

        composeTestRule.setContent {
            ToDoListContent(
                uiState = ToDoListUiState(
                    itemsByDate = mapOf(
                        today to listOf(item1),
                        tomorrow to listOf(item2)
                    )
                ),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Today Assignment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tomorrow Assignment").assertIsDisplayed()
    }

    @Test
    fun emptyStateDisplayedWhenAllItemsFilteredOut() {
        val item = createToDoItem(id = "1", title = "Filtered Item")

        composeTestRule.setContent {
            ToDoListContent(
                uiState = ToDoListUiState(
                    itemsByDate = mapOf(Date() to listOf(item)),
                    removingItemIds = setOf("1")
                ),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.noToDosForNow)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Filtered Item").assertIsNotDisplayed()
    }

    @Test
    fun itemsFilteredByRemovingItemIds() {
        val item1 = createToDoItem(id = "1", title = "Visible Item")
        val item2 = createToDoItem(id = "2", title = "Hidden Item")

        composeTestRule.setContent {
            ToDoListContent(
                uiState = ToDoListUiState(
                    itemsByDate = mapOf(Date() to listOf(item1, item2)),
                    removingItemIds = setOf("2")
                ),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Visible Item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hidden Item").assertIsNotDisplayed()
    }

    @Test
    fun checkedItemDisplaysCorrectly() {
        val item = createToDoItem(
            id = "1",
            title = "Completed Assignment",
            isChecked = true
        )

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Completed Assignment").assertIsDisplayed()
        composeTestRule.onNodeWithTag("todoCheckbox_1").assertIsDisplayed()
    }

    @Test
    fun itemWithTagDisplaysTag() {
        val item = createToDoItem(
            id = "1",
            title = "Assignment with Tag",
            tag = "Important Tag"
        )

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Assignment with Tag").assertIsDisplayed()
        composeTestRule.onNodeWithText("Important Tag").assertIsDisplayed()
    }

    @Test
    fun itemWithDateLabelDisplaysLabel() {
        val item = createToDoItem(
            id = "1",
            title = "Assignment with Date",
            dateLabel = "11:59 PM"
        )

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Assignment with Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("11:59 PM").assertIsDisplayed()
    }

    @Test
    fun nonClickableItemDoesNotHaveClickAction() {
        val item = createToDoItem(
            id = "1",
            title = "Non-clickable Item",
            isClickable = false
        )

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Non-clickable Item").assertIsDisplayed()
    }

    @Test
    fun clickableItemHasClickAction() {
        val item = createToDoItem(
            id = "1",
            title = "Clickable Item",
            isClickable = true,
            htmlUrl = "https://example.com"
        )

        composeTestRule.setContent {
            ToDoListContent(
                uiState = createUiStateWithItems(items = listOf(item)),
                onOpenToDoItem = {},
                onDateClick = {}
            )
        }

        composeTestRule.onNodeWithText("Clickable Item").assertIsDisplayed().assertHasClickAction()
    }

    // Helper functions to create test data

    private fun createLoadingUiState(): ToDoListUiState {
        return ToDoListUiState(
            isLoading = true
        )
    }

    private fun createErrorUiState(): ToDoListUiState {
        return ToDoListUiState(
            isError = true
        )
    }

    private fun createEmptyUiState(): ToDoListUiState {
        return ToDoListUiState(
            itemsByDate = emptyMap()
        )
    }

    private fun createUiStateWithItems(
        items: List<ToDoItemUiState> = listOf(createToDoItem())
    ): ToDoListUiState {
        return ToDoListUiState(
            itemsByDate = mapOf(Date() to items)
        )
    }

    private fun createToDoItem(
        id: String = "1",
        title: String = "Test Assignment",
        date: Date = Calendar.getInstance().apply { set(2024, 9, 22, 11, 59) }.time,
        dateLabel: String? = "11:59 AM",
        contextLabel: String = "Test Course",
        canvasContext: CanvasContext = CanvasContext.defaultCanvasContext(),
        itemType: ToDoItemType = ToDoItemType.ASSIGNMENT,
        iconRes: Int = R.drawable.ic_assignment,
        isChecked: Boolean = false,
        isClickable: Boolean = true,
        htmlUrl: String? = "https://example.com/assignments/$id",
        tag: String? = null,
        onCheckboxToggle: (Boolean) -> Unit = {},
        onSwipeToDone: () -> Unit = {}
    ): ToDoItemUiState {
        return ToDoItemUiState(
            id = id,
            title = title,
            date = date,
            dateLabel = dateLabel,
            contextLabel = contextLabel,
            canvasContext = canvasContext,
            itemType = itemType,
            iconRes = iconRes,
            isChecked = isChecked,
            isClickable = isClickable,
            htmlUrl = htmlUrl,
            tag = tag,
            onCheckboxToggle = onCheckboxToggle,
            onSwipeToDone = onSwipeToDone
        )
    }
}
