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
package com.instructure.student.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.common.interaction.InboxSignatureInteractionTest
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionCommentsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionRubricManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.student.BuildConfig
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.LeftSideNavigationDrawerPage
import com.instructure.student.ui.utils.StudentActivityTestRule
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers

@HiltAndroidTest
@UninstallModules(GraphQlApiModule::class)
class StudentInboxSignatureInteractionTest : InboxSignatureInteractionTest() {

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @BindValue
    @JvmField
    val assignmentDetailsManager: AssignmentDetailsManager = FakeAssignmentDetailsManager()

    @BindValue
    @JvmField
    val submissionContentManager: SubmissionContentManager = FakeSubmissionContentManager()

    @BindValue
    @JvmField
    val submissionGradeManager: SubmissionGradeManager = FakeSubmissionGradeManager()

    @BindValue
    @JvmField
    val submissionRubricManager: SubmissionRubricManager = FakeSubmissionRubricManager()

    @BindValue
    @JvmField
    val submissionDetailsManager: SubmissionDetailsManager = FakeSubmissionDetailsManager()

    @BindValue
    @JvmField
    val submissionCommentsManager: SubmissionCommentsManager = FakeSubmissionCommentsManager()

    private val leftSideNavigationDrawerPage = LeftSideNavigationDrawerPage()

    override val activityRule = StudentActivityTestRule(LoginActivity::class.java)

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
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Inbox Signature")

        composeTestRule.waitForIdle()
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }
}