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
package com.instructure.teacher.ui

import com.instructure.canvas.espresso.common.interaction.InboxSignatureInteractionTest
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.ui.pages.DashboardPage
import com.instructure.teacher.ui.pages.LeftSideNavigationDrawerPage
import com.instructure.teacher.ui.utils.TeacherActivityTestRule
import com.instructure.teacher.ui.utils.openLeftSideMenu
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules

@HiltAndroidTest
@UninstallModules(GraphQlApiModule::class)
class TeacherInboxSignatureInteractionTest : InboxSignatureInteractionTest() {

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @BindValue
    @JvmField
    val personContextManager: StudentContextManager = FakeStudentContextManager()

    @BindValue
    @JvmField
    val assignmentDetailsManager: AssignmentDetailsManager = FakeAssignmentDetailsManager()

    @BindValue
    @JvmField
    val submissionContentManager: SubmissionContentManager = FakeSubmissionContentManager()

    @BindValue
    @JvmField
    val submissionGradeManager: SubmissionGradeManager = FakeSubmissionGradeManager()

    private val dashboardPage = DashboardPage()
    private val leftSideNavigationDrawerPage = LeftSideNavigationDrawerPage()

    override val activityRule = TeacherActivityTestRule(LoginActivity::class.java)

    override val isTesting: Boolean = BuildConfig.IS_TESTING

    override fun initData(): MockCanvas {
        return MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 2,
            favoriteCourseCount = 1
        )
    }

    override fun goToInboxSignatureSettings(data: MockCanvas) {
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openLeftSideMenu()
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Inbox Signature")

        composeTestRule.waitForIdle()
    }
}