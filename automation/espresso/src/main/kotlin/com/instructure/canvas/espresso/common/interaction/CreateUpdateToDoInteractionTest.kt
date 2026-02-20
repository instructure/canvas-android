/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoCreateUpdatePage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoDetailsPage
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addPlannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.User
import org.junit.Test
import java.util.Calendar
import java.util.Date


abstract class CreateUpdateToDoInteractionTest : CanvasComposeTest() {

    val calendarScreenPage = CalendarScreenPage(composeTestRule)
    val calendarToDoDetailsPage = CalendarToDoDetailsPage(composeTestRule)
    private val calendarToDoCreateUpdatePage = CalendarToDoCreateUpdatePage(composeTestRule)

    @Test
    fun assertNewTitle() {
        val data = initData()

        goToCreateToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("New Todo")
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.assertItemDetails("New Todo", "To Do")
    }

    @Test
    fun assertNewDate() {
        val data = initData()

        goToCreateToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("New Todo")
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        calendarToDoCreateUpdatePage.selectDate(calendar)
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.assertItemDetails("New Todo", "To Do")
    }

    @Test
    fun assertNewTime() {
        val data = initData()

        goToCreateToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("New Todo")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 15)
        }
        calendarToDoCreateUpdatePage.selectTime(calendar)
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("New Todo")
        calendarToDoDetailsPage.assertDate(activityRule.activity, calendar.time)
    }

    @Test
    fun assertNewCanvasContext() {
        val data = initData()

        goToCreateToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("New Todo")
        val canvasContextName = data.courses.values.first().name
        calendarToDoCreateUpdatePage.selectCanvasContext(canvasContextName)
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.assertItemDetails("New Todo", "$canvasContextName To Do")
    }

    @Test
    fun assertNewDetails() {
        val data = initData()

        goToCreateToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("New Todo")
        calendarToDoCreateUpdatePage.typeDetails("New Details")
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("New Todo")
        calendarToDoDetailsPage.assertDescription("New Details")
    }

    @Test
    fun assertUpdatedTitle() {
        val data = initData()
        val user = getLoggedInUser()
        data.addPlannable(
            name = "Test Todo",
            course = null,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date(),
            details = "Test Description"
        )

        goToEditToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.assertPageTitle("Edit To Do")
        calendarToDoCreateUpdatePage.assertTodoTitle("Test Todo")
        calendarToDoCreateUpdatePage.typeTodoTitle("Updated Title")
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.assertItemDetails("Updated Title", "To Do")
    }

    @Test
    fun assertUpdatedDate() {
        val data = initData()
        val user = getLoggedInUser()
        val date = Date()
        data.addPlannable(
            name = "Test Todo",
            course = null,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = date,
            details = "Test Description"
        )

        goToEditToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.assertDate(date)
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        calendarToDoCreateUpdatePage.selectDate(calendar)
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.swipeEventsLeft()
        calendarScreenPage.assertItemDetails("Test Todo", "To Do")
    }

    @Test
    fun assertUpdatedTime() {
        val data = initData()
        val user = getLoggedInUser()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 15)
        }
        data.addPlannable(
            name = "Test Todo",
            course = null,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = calendar.time,
            details = "Test Description"
        )

        goToEditToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.assertTime(activityRule.activity, calendar.time)
        val updatedCalendar = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
        calendarToDoCreateUpdatePage.selectTime(updatedCalendar)
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("Test Todo")
        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.assertDate(activityRule.activity, updatedCalendar.time)
    }

    @Test
    fun assertUpdatedCanvasContext() {
        val data = initData()
        val user = getLoggedInUser()
        data.addPlannable(
            name = "Test Todo",
            course = null,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date(),
            details = "Test Description"
        )

        goToEditToDo(data)

        composeTestRule.waitForIdle()
        val canvasContextName = data.courses.values.first().name
        calendarToDoCreateUpdatePage.assertCanvasContext(user.name)
        calendarToDoCreateUpdatePage.selectCanvasContext(canvasContextName)
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.assertItemDetails("Test Todo", "$canvasContextName To Do")
    }

    @Test
    fun assertUpdatedDetails() {
        val data = initData()
        val user = getLoggedInUser()
        val date = Date()
        data.addPlannable(
            name = "Test Todo",
            course = null,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = date,
            details = "Test Description"
        )

        goToEditToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.assertDetails("Test Description")
        calendarToDoCreateUpdatePage.typeDetails("Updated Description")
        calendarToDoCreateUpdatePage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("Test Todo")
        calendarToDoDetailsPage.assertDescription("Updated Description")
    }

    @Test
    fun assertUnsavedChangesDialog() {
        val data = initData()
        val user = getLoggedInUser()
        data.addPlannable(
            name = "Test Todo",
            course = null,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date(),
            details = "Test Description"
        )

        goToEditToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("Updated Title")
        calendarToDoCreateUpdatePage.clickClose()

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.assertUnsavedChangesDialog()
    }

    @Test
    fun saveDisabledWhenTitleBlank() {
        val data = initData()
        goToCreateToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("  ")
        calendarToDoCreateUpdatePage.assertSaveDisabled()
    }

    @Test
    fun saveEnabledWhenTitleIsNotBlank() {
        val data = initData()
        goToCreateToDo(data)

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.typeTodoTitle("New Title")
        calendarToDoCreateUpdatePage.assertSaveEnabled()
    }

    abstract fun goToCreateToDo(data: MockCanvas)

    abstract fun goToEditToDo(data: MockCanvas)

    abstract fun initData(): MockCanvas

    abstract fun getLoggedInUser(): User

    override fun displaysPageObjects() = Unit

}
