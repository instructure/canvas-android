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
 */package com.instructure.canvas.espresso.common

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.page.ToDoDetailsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addPlannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.pandautils.utils.textAndIconColor
import org.junit.Test
import java.util.Date

abstract class ToDoDetailsInteractionTest : CanvasComposeTest() {

    private val toDoDetailsPage = ToDoDetailsPage(composeTestRule)

    @Test
    fun assertTitle() {
        val data = initData()
        val student = data.students[0]
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = student.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        toDoDetailsPage.assertTitle(data.todos.first().plannable.title)
    }

    @Test
    fun assertCanvasContext() {
        val data = initData()
        val student = data.students[0]
        val course = data.courses.values.first()
        data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = student.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoDetails(data)

        composeTestRule.waitForIdle()
        toDoDetailsPage.waitForToolbar()
        toDoDetailsPage.assertCanvasContext(course.name, course.textAndIconColor)
    }

    abstract fun goToToDoDetails(data: MockCanvas)

    abstract fun initData(): MockCanvas
}