/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.compose

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.RemoteConfigOverrides
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.FeatureFlagsApi
import com.instructure.dataseeding.api.GlobalAnnouncementsApi
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.FEATURE_FLAG_WIDGET_DASHBOARD
import com.instructure.student.ui.pages.classic.WebViewTextCheck
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.di.FakeTimeProvider
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLoginWithoutDashboardWait
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * End-to-end demonstration that the redesigned dashboard (gated by the `DASHBOARD_REDESIGN` Firebase
 * Remote Config flag) can be exercised in E2E tests (MBL-19979).
 *
 * The new dashboard requires all of: the `DASHBOARD_REDESIGN` remote config flag, the server-side
 * `widget_dashboard` Canvas feature flag, and the (default-on) widget config. This test controls the
 * first via [RemoteConfigOverrides] and the second by enabling the account feature flag through the
 * data-seeding API, then logs in and asserts the Compose dashboard is shown.
 */
@HiltAndroidTest
class NewDashboardE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    // Ids of the global announcements seeded during a test, tracked so tearDown can delete them from the
    // shared account regardless of whether the test passed or failed (the in-app dismiss only closes them
    // per-user).
    private val seededGlobalAnnouncementIds = mutableListOf<Long>()

    @Before
    fun setUp() {
        // Set the flags this test needs up front: the server-side widget_dashboard Canvas flag and
        // the DASHBOARD_REDESIGN remote config override.
        Log.d(PREPARATION_TAG, "Enable the '$FEATURE_FLAG_WIDGET_DASHBOARD' Canvas feature flag on the account.")
        FeatureFlagsApi.setAccountFeatureFlag(feature = FEATURE_FLAG_WIDGET_DASHBOARD, state = "on")

        Log.d(PREPARATION_TAG, "Override the DASHBOARD_REDESIGN remote config flag (default 'false') to 'true'.")
        RemoteConfigOverrides.set(RemoteConfigParam.DASHBOARD_REDESIGN, true)
    }

    @After
    fun tearDown() {
        // Reset the flags set in setUp so they don't leak into other tests/runs on the shared E2E
        // instance. (performReset also clears the remote config override, but reset it here too so
        // both flags are torn down in one place.)
        FeatureFlagsApi.setAccountFeatureFlag(feature = FEATURE_FLAG_WIDGET_DASHBOARD, state = "off")
        RemoteConfigOverrides.clear()
        FakeTimeProvider.reset()

        seededGlobalAnnouncementIds.forEach { announcementId ->
            Log.d(PREPARATION_TAG, "Clean up: delete the seeded global announcement (id: $announcementId) from the account so it doesn't accumulate on the shared instance.")
            try {
                GlobalAnnouncementsApi.deleteGlobalAnnouncement(announcementId)
            } catch (e: Exception) {
                Log.w(PREPARATION_TAG, "Failed to delete seeded global announcement (id: $announcementId): ${e.message}")
            }
        }
        seededGlobalAnnouncementIds.clear()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.NEW_DASHBOARD, TestCategory.E2E)
    fun testWelcomeWidgetE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        FakeTimeProvider.fakeHourOfDay = 12

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginWithoutDashboardWait(student)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the Welcome widget shows an encouragement message.")
        newDashboardPage.assertWelcomeMessageDisplayed()

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        Log.d(ASSERTION_TAG, "Assert that the initial (login-time) greeting matches the afternoon bucket, i.e. no refresh needed.")
        val initialGreeting = context.getString(R.string.welcomeGreetingAfternoonWithName, student.shortName)
        newDashboardPage.assertWelcomeGreetingDisplayed(initialGreeting)

        val greetingScenarios = listOf(
            9 to R.string.welcomeGreetingMorningWithName,
            19 to R.string.welcomeGreetingEveningWithName,
            23 to R.string.welcomeGreetingNightWithName
        )

        greetingScenarios.forEach { (hourOfDay, expectedGreetingRes) ->
            Log.d(STEP_TAG, "Force the device hour-of-day to $hourOfDay and refresh the dashboard.")
            FakeTimeProvider.fakeHourOfDay = hourOfDay
            newDashboardPage.refreshDashboard()

            Log.d(ASSERTION_TAG, "Assert that the Welcome widget shows the greeting for hour $hourOfDay.")
            val expectedGreeting = context.getString(expectedGreetingRes, student.shortName)
            newDashboardPage.assertWelcomeGreetingDisplayed(expectedGreeting)

            Log.d(ASSERTION_TAG, "Assert that the Welcome widget shows an encouragement message.")
            newDashboardPage.assertWelcomeMessageDisplayed()
        }
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.NEW_DASHBOARD, TestCategory.E2E)
    fun testCourseInvitationsWidgetE2E() {

        Log.d(PREPARATION_TAG, "Seed a student, a teacher and an active course the student is already enrolled in.")
        val data = seedData(students = 1, teachers = 1, courses = 1, favoriteCourses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val activeCourse = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Create two more courses to invite the student into.")
        val courseToAccept = CoursesApi.createCourse()
        val courseToDecline = CoursesApi.createCourse()

        Log.d(PREPARATION_TAG, "Enroll the teacher as an active teacher in both invitation courses.")
        EnrollmentsApi.enrollUserAsTeacher(courseToAccept.id, teacher.id)
        EnrollmentsApi.enrollUserAsTeacher(courseToDecline.id, teacher.id)

        Log.d(PREPARATION_TAG, "Seed a pending (invited) student enrollment in both invitation courses.")
        EnrollmentsApi.enrollUserAsPendingStudent(courseToAccept.id, student.id)
        EnrollmentsApi.enrollUserAsPendingStudent(courseToDecline.id, student.id)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginWithoutDashboardWait(student)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the already-enrolled course '${activeCourse.name}' shows in the Courses widget, with a course count of 1 in the header.")
        newDashboardPage.assertCourseDisplayed(activeCourse.id)
        newDashboardPage.assertCoursesHeaderCount(1)

        Log.d(ASSERTION_TAG, "Assert that both seeded course invitations are displayed on the dashboard, with a count of 2 in the header.")
        newDashboardPage.assertCourseInvitationsHeaderCount(2)
        newDashboardPage.assertCourseInvitationDisplayed(courseToAccept.id, courseToAccept.name)
        newDashboardPage.assertCourseInvitationDisplayed(courseToDecline.id, courseToDecline.name)

        Log.d(STEP_TAG, "Decline the invitation to '${courseToDecline.name}'.")
        newDashboardPage.declineCourseInvitation(courseToDecline.id)

        Log.d(ASSERTION_TAG, "Assert that the declined invitation is no longer displayed on the dashboard, and the header count drops to 1.")
        newDashboardPage.assertCourseInvitationGone(courseToDecline.id)
        newDashboardPage.assertCourseInvitationsHeaderCount(1)

        Log.d(ASSERTION_TAG, "Assert that the remaining invitation to '${courseToAccept.name}' is still displayed.")
        newDashboardPage.assertCourseInvitationDisplayed(courseToAccept.id, courseToAccept.name)

        Log.d(STEP_TAG, "Accept the invitation to '${courseToAccept.name}'.")
        newDashboardPage.acceptCourseInvitation(courseToAccept.id)

        Log.d(ASSERTION_TAG, "Assert that the accepted invitation is no longer displayed on the dashboard.")
        newDashboardPage.assertCourseInvitationGone(courseToAccept.id)

        Log.d(STEP_TAG, "Refresh the dashboard so the Courses widget reflects the newly accepted enrollment.")
        newDashboardPage.refreshDashboard()

        Log.d(ASSERTION_TAG, "Assert that the accepted course '${courseToAccept.name}' now shows in the Courses widget alongside the originally enrolled course '${activeCourse.name}', with the header count rising to 2.")
        newDashboardPage.assertCourseDisplayed(courseToAccept.id)
        newDashboardPage.assertCourseDisplayed(activeCourse.id)
        newDashboardPage.assertCoursesHeaderCount(2)

        Log.d(ASSERTION_TAG, "Assert that the declined course '${courseToDecline.name}' does not show in the Courses widget.")
        newDashboardPage.assertCourseNotDisplayed(courseToDecline.id)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.NEW_DASHBOARD, TestCategory.E2E)
    fun testGlobalAnnouncementWidgetE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]

        val timestamp = System.currentTimeMillis()
        val subjectToOpen = "E2E Global Announcement To Open $timestamp"
        val messageToOpen = "This is the seeded global announcement body content to open."
        val subjectToDismiss = "E2E Global Announcement To Dismiss $timestamp"
        val messageToDismiss = "This is the seeded global announcement body content to dismiss."

        Log.d(PREPARATION_TAG, "Seed two global (institutional) announcements with subjects: '$subjectToOpen' and '$subjectToDismiss'.")
        val announcementToOpen = GlobalAnnouncementsApi.createGlobalAnnouncement(subject = subjectToOpen, message = messageToOpen)
        seededGlobalAnnouncementIds.add(announcementToOpen.id)
        val announcementToDismiss = GlobalAnnouncementsApi.createGlobalAnnouncement(subject = subjectToDismiss, message = messageToDismiss)
        seededGlobalAnnouncementIds.add(announcementToDismiss.id)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginWithoutDashboardWait(student)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the Global Announcements widget shows both seeded announcements (swiping the pager as needed), with a count of 2 in the header.")
        newDashboardPage.assertGlobalAnnouncementsHeaderCount(2)
        newDashboardPage.assertGlobalAnnouncementDisplayed(announcementToOpen.id, subjectToOpen)
        newDashboardPage.assertGlobalAnnouncementDisplayed(announcementToDismiss.id, subjectToDismiss)

        Log.d(STEP_TAG, "Tap the '$subjectToDismiss' announcement.")
        newDashboardPage.clickGlobalAnnouncement(announcementToDismiss.id)

        Log.d(ASSERTION_TAG, "Assert that the announcement detail view (WebView) opens with the subject as its title and the seeded message as its content.")
        canvasWebViewPage.waitForWebView()
        canvasWebViewPage.assertTitle(subjectToDismiss)
        canvasWebViewPage.runTextChecks(WebViewTextCheck(Locator.TAG_NAME, "body", messageToDismiss, repeatSecs = 30))

        Log.d(STEP_TAG, "Navigate back to the dashboard.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed again.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(STEP_TAG, "Dismiss the '$subjectToDismiss' announcement.")
        newDashboardPage.dismissGlobalAnnouncement(announcementToDismiss.id)

        Log.d(ASSERTION_TAG, "Assert that the dismissed announcement is no longer displayed on the dashboard, and the header count drops to 1.")
        newDashboardPage.assertGlobalAnnouncementGone(announcementToDismiss.id)
        newDashboardPage.assertGlobalAnnouncementsHeaderCount(1)

        Log.d(STEP_TAG, "Tap the remaining '$subjectToOpen' announcement.")
        newDashboardPage.clickGlobalAnnouncement(announcementToOpen.id)

        Log.d(ASSERTION_TAG, "Assert that its detail view (WebView) opens with the subject as its title and the seeded message as its content.")
        canvasWebViewPage.waitForWebView()
        canvasWebViewPage.assertTitle(subjectToOpen)
        canvasWebViewPage.runTextChecks(WebViewTextCheck(Locator.TAG_NAME, "body", messageToOpen, repeatSecs = 30))

        Log.d(STEP_TAG, "Navigate back to the dashboard.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed again.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(STEP_TAG, "Refresh the dashboard.")
        newDashboardPage.refreshDashboard()

        Log.d(ASSERTION_TAG, "Assert that the remaining '$subjectToOpen' announcement is still displayed after refreshing, and the dismissed one stays gone.")
        newDashboardPage.assertGlobalAnnouncementsHeaderCount(1)
        newDashboardPage.assertGlobalAnnouncementDisplayed(announcementToOpen.id, subjectToOpen)
        newDashboardPage.assertGlobalAnnouncementGone(announcementToDismiss.id)
    }
}
