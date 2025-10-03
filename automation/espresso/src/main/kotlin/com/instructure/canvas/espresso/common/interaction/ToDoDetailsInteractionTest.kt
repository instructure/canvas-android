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
 */
package com.instructure.canvas.espresso.common.interaction

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoCreateUpdatePage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoDetailsPage
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addPlannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.toDate
import org.junit.Test
import java.util.Date

abstract class ToDoDetailsInteractionTest : CanvasComposeTest() {

    val calendarScreenPage = CalendarScreenPage(composeTestRule)
    private val calendarToDoDetailsPage = CalendarToDoDetailsPage(composeTestRule)
    private val calendarToDoCreateUpdatePage = CalendarToDoCreateUpdatePage(composeTestRule)

    @Test
    fun assertTitle() {
        val data = initData()
        val user = getLoggedInUser()
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.assertTitle(data.todos.first().plannable.title)
    }

    @Test
    fun assertCanvasContext() {
        val data = initData()
        val user = getLoggedInUser()
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.assertCanvasContext(course.name)
    }

    @Test
    fun assertDate() {
        val data = initData()
        val user = getLoggedInUser()
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.assertDate(originalActivity, data.todos.first().plannable.todoDate.toDate()!!)
    }

    @Test
    fun assertDescription() {
        val data = initData()
        val user = getLoggedInUser()
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date(),
            details = "Test Description"
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.assertDescription("Test Description")
    }

    @Test
    fun openToDoEditPage() {
        val data = initData()
        val user = getLoggedInUser()
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.clickToolbarMenu()
        calendarToDoDetailsPage.clickEditMenu()

        composeTestRule.waitForIdle()
        calendarToDoCreateUpdatePage.assertPageTitle("Edit To Do")
    }

    @Test
    fun openToDoEditDialog() {
        val data = initData()
        val user = getLoggedInUser()
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.clickToolbarMenu()
        calendarToDoDetailsPage.clickDeleteMenu()

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.assertDeleteDialog()
    }

    @Test
    fun deleteToDo() {
        val data = initData()
        val user = getLoggedInUser()
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        calendarToDoDetailsPage.clickToolbarMenu()
        calendarToDoDetailsPage.clickDeleteMenu()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Delete To Do?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.waitForIdle()
        calendarScreenPage.assertItemNotExist("Test Todo")
    }

    abstract fun goToToDoDetails(data: MockCanvas)

    abstract fun initData(): MockCanvas

    abstract fun getLoggedInUser(): User
}