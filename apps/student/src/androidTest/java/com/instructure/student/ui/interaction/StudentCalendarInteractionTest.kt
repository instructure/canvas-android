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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.common.interaction.CalendarInteractionTest
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.User
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.DashboardPage
import com.instructure.student.ui.pages.DiscussionDetailsPage
import com.instructure.student.ui.utils.StudentActivityTestRule
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class StudentCalendarInteractionTest : CalendarInteractionTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule = StudentActivityTestRule(LoginActivity::class.java)

    private val dashboardPage = DashboardPage()
    private val assignmentDetailsPage = AssignmentDetailsPage(ModuleItemInteractions())
    private val discussionDetailsPage = DiscussionDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))

    override fun goToCalendar(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        dashboardPage.clickCalendarTab()

        composeTestRule.waitForIdle()
    }

    override fun initData(): MockCanvas {
        return MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 2,
            favoriteCourseCount = 1
        )
    }

    override fun getLoggedInUser(): User {
        return MockCanvas.data.students[0]
    }

    override fun assertAssignmentDetailsTitle(title: String) {
        assignmentDetailsPage.assertAssignmentTitle(title)
    }

    override fun assertDiscussionDetailsTitle(title: String) {
        discussionDetailsPage.assertToolbarDiscussionTitle(title)
    }

    override fun clickTodayButton() {
        calendarScreenPage.clickTodayButton()
    }
}