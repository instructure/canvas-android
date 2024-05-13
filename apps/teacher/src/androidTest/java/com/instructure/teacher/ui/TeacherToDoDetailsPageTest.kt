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
package com.instructure.teacher.ui

import android.app.Activity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import com.instructure.canvas.espresso.common.ToDoDetailsInteractionTest
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.ui.pages.DashboardPage
import com.instructure.teacher.ui.utils.TeacherActivityTestRule
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest

@HiltAndroidTest
class TeacherToDoDetailsPageTest : ToDoDetailsInteractionTest() {

    override val activityRule: InstructureActivityTestRule<out Activity>
            = TeacherActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    val dashboardPage = DashboardPage()

    override fun displaysPageObjects() = Unit

    override fun goToToDoDetails(data: MockCanvas) {
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCalendar()

        val todo = data.todos.first()

        //TODO: Update when the CalendarPage is ready
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText(todo.plannable.title)).performClick()
    }

    override fun initData(): MockCanvas {
        return MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 1,
            favoriteCourseCount = 1
        )
    }
}