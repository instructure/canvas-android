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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.common.interaction.InboxListInteractionTest
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addRecipientsToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.User
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.ui.pages.classic.DashboardPage
import com.instructure.teacher.ui.utils.TeacherActivityTestRule
import com.instructure.teacher.ui.utils.extensions.clickInboxTab
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest

@HiltAndroidTest
class TeacherInboxListInteractionTest : InboxListInteractionTest() {

    override val isTesting = BuildConfig.IS_TESTING
    override val activityRule = TeacherActivityTestRule(LoginActivity::class.java)

    private val dashboardPage = DashboardPage()

    override fun goToInbox(data: MockCanvas) {
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.clickInboxTab()
    }

    override fun createInitialData(courseCount: Int): MockCanvas {
        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            teacherCount = 1,
            favoriteCourseCount = courseCount
        )

        val course1 = data.courses.values.first()

        data.addCoursePermissions(
            course1.id,
            CanvasContextPermission(send_messages_all = true, send_messages = true)
        )

        data.addRecipientsToCourse(
            course = course1,
            students = data.students,
            teachers = data.teachers
        )

        return data
    }

    override fun getLoggedInUser(): User {
        return MockCanvas.data.teachers[0]
    }

    override fun getOtherUser(): User {
        return MockCanvas.data.students[0]
    }
}