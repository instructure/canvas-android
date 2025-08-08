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
 */    package com.instructure.parentapp.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignmentsToGroups
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addObserverAlert
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.AlertWorkflowState
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Test
import java.util.Date

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class AlertsInteractionTest : ParentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    @Test
    fun dismissAlert() {
        val data = initData()
        goToAlerts(data)

        val student = data.students.first()
        val observer = data.parents.first()
        val course = data.courses.values.first()

        val alert = data.addObserverAlert(
            observer,
            student,
            course,
            AlertType.ASSIGNMENT_MISSING,
            AlertWorkflowState.UNREAD,
            Date(),
            null,
            false
        )

        alertsPage.refresh()

        alertsPage.assertAlertItemDisplayed(alert.title)

        composeTestRule.waitForIdle()
        alertsPage.dismissAlert(alert.title)

        composeTestRule.waitForIdle()
        alertsPage.assertSnackbar("Alert dismissed")
        alertsPage.assertAlertItemNotDisplayed(alert.title)
        alertsPage.refresh()
        alertsPage.assertAlertItemNotDisplayed(alert.title)
    }

    @Test
    fun undoDismiss() {
        val data = initData()
        goToAlerts(data)

        val student = data.students.first()
        val observer = data.parents.first()
        val course = data.courses.values.first()

        val alert = data.addObserverAlert(
            observer,
            student,
            course,
            AlertType.ASSIGNMENT_MISSING,
            AlertWorkflowState.UNREAD,
            Date(),
            null,
            false
        )

        alertsPage.refresh()

        alertsPage.assertAlertItemDisplayed(alert.title)

        composeTestRule.waitForIdle()
        alertsPage.dismissAlert(alert.title)

        composeTestRule.waitForIdle()
        alertsPage.assertSnackbar("Alert dismissed")
        alertsPage.clickUndo()
        alertsPage.assertAlertItemDisplayed(alert.title)

        alertsPage.refresh()
        alertsPage.assertAlertItemDisplayed(alert.title)
    }

    @Test
    fun emptyAlerts() {
        val data = initData()
        goToAlerts(data)

        alertsPage.assertEmptyState()
    }

    @Test
    fun openAssignmentAlert() {
        val data = initData()
        goToAlerts(data)

        val student = data.students.first()
        val observer = data.parents.first()
        val course = data.courses.values.first()
        data.addAssignmentsToGroups(course)
        val assignment = data.assignments.values.first { it.submission != null }

        val alert = data.addObserverAlert(
            observer,
            student,
            course,
            AlertType.ASSIGNMENT_MISSING,
            AlertWorkflowState.UNREAD,
            Date(),
            "https://${data.domain}/courses/${course.id}/assignments/${assignment.id}",
            false
        )

        alertsPage.refresh()

        composeTestRule.waitForIdle()
        alertsPage.assertAlertItemDisplayed(alert.title)
        alertsPage.assertAlertUnread(alert.title)
        alertsPage.clickOnAlert(alert.title)

        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusSubmitted()
    }

    @Test
    fun openCourseAlert() {
        val data = MockCanvas.init(studentCount = 1, parentCount = 1, courseCount = 1, teacherCount = 1)
        goToAlerts(data)

        val student = data.students.first()
        val observer = data.parents.first()
        val teacher = data.teachers.first()
        val course = data.courses.values.first()

        data.addCoursePermissions(
            course.id,
            CanvasContextPermission(canCreateAnnouncement = true)
        )

        val announcement = data.addDiscussionTopicToCourse(
            course = course,
            user = teacher,
            isAnnouncement = true
        )

        val alert = data.addObserverAlert(
            observer,
            student,
            course,
            AlertType.COURSE_ANNOUNCEMENT,
            AlertWorkflowState.UNREAD,
            Date(),
            "https://${data.domain}/courses/${course.id}/discussion_topics/${announcement.id}",
            false
        )

        alertsPage.refresh()

        composeTestRule.waitForIdle()
        alertsPage.assertAlertItemDisplayed(alert.title)
        alertsPage.assertAlertUnread(alert.title)
        alertsPage.clickOnAlert(alert.title)

        announcementDetailsPage.assertCourseAnnouncementDetailsDisplayed(course, announcement)
    }

    @Test
    fun openGlobalAlert() {
        val data = MockCanvas.init(studentCount = 1, parentCount = 1, teacherCount = 1, courseCount = 1, accountNotificationCount = 1)
        goToAlerts(data)

        val student = data.students.first()
        val observer = data.parents.first()
        val announcement = data.accountNotifications.values.first()

        val alert = data.addObserverAlert(
            observer,
            student,
            CanvasContext.getGenericContext(CanvasContext.Type.USER, announcement.id),
            AlertType.INSTITUTION_ANNOUNCEMENT,
            AlertWorkflowState.UNREAD,
            Date(),
            null,
            false
        )

        alertsPage.refresh()

        composeTestRule.waitForIdle()
        alertsPage.assertAlertItemDisplayed(alert.title)
        alertsPage.assertAlertUnread(alert.title)
        alertsPage.clickOnAlert(alert.title)

        announcementDetailsPage.assertGlobalAnnouncementDetailsDisplayed(announcement)
    }

    private fun initData(): MockCanvas {
        return MockCanvas.init(studentCount = 1, parentCount = 1, courseCount = 1)
    }

    private fun goToAlerts(data: MockCanvas) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)

        dashboardPage.clickAlertsBottomMenu()
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