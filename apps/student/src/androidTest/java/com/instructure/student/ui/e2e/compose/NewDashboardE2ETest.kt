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
import com.instructure.canvas.espresso.common.pages.compose.CustomizeDashboardPage
import com.instructure.canvas.espresso.utils.pressBackButton
import com.instructure.canvas.espresso.utils.refresh
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.FeatureFlagsApi
import com.instructure.dataseeding.api.GlobalAnnouncementsApi
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.utils.FEATURE_FLAG_WIDGET_DASHBOARD
import com.instructure.student.ui.pages.classic.ConferenceWebPage
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

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.NEW_DASHBOARD, TestCategory.E2E)
    fun testCustomizeDashboardE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, favoriteCourses = 1)
        val student = data.studentsList[0]

        val welcomeWidget = WidgetMetadata.WIDGET_ID_WELCOME
        val coursesWidget = WidgetMetadata.WIDGET_ID_COURSES
        val forecastWidget = WidgetMetadata.WIDGET_ID_FORECAST
        val todoWidget = WidgetMetadata.WIDGET_ID_TODO

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginWithoutDashboardWait(student)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the customizable widgets are displayed on the dashboard in their default order.")
        newDashboardPage.assertWidgetOrder(welcomeWidget, coursesWidget, forecastWidget, todoWidget)

        Log.d(STEP_TAG, "Tap the 'Customize Dashboard' button at the bottom of the dashboard.")
        newDashboardPage.clickCustomizeDashboardButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Customize Dashboard' page is displayed.")
        customizeDashboardPage.assertPageDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all four customizable widgets are listed in their default order, with their visibility switches turned on.")
        customizeDashboardPage.assertWidgetOrder(welcomeWidget, coursesWidget, forecastWidget, todoWidget)
        customizeDashboardPage.assertWidgetVisibilityOn(welcomeWidget)
        customizeDashboardPage.assertWidgetVisibilityOn(coursesWidget)
        customizeDashboardPage.assertWidgetVisibilityOn(forecastWidget)
        customizeDashboardPage.assertWidgetVisibilityOn(todoWidget)

        Log.d(ASSERTION_TAG, "Assert that the first widget cannot be moved up and the last one cannot be moved down.")
        customizeDashboardPage.assertMoveUpButtonDisabled(welcomeWidget)
        customizeDashboardPage.assertMoveDownButtonDisabled(todoWidget)

        Log.d(STEP_TAG, "Turn off the visibility switch of the 'Weekly Summary' (forecast) widget.")
        customizeDashboardPage.toggleWidgetVisibility(forecastWidget)

        Log.d(ASSERTION_TAG, "Assert that the 'Weekly Summary' widget's visibility switch is turned off.")
        customizeDashboardPage.assertWidgetVisibilityOff(forecastWidget)

        Log.d(ASSERTION_TAG, "Assert that the hidden 'Weekly Summary' widget moved one position down, below the still visible 'To Do' widget.")
        customizeDashboardPage.assertWidgetOrder(welcomeWidget, coursesWidget, todoWidget, forecastWidget)

        Log.d(STEP_TAG, "Turn off the visibility switch of the 'To Do' widget.")
        customizeDashboardPage.toggleWidgetVisibility(todoWidget)

        Log.d(ASSERTION_TAG, "Assert that the 'To Do' widget's visibility switch is turned off.")
        customizeDashboardPage.assertWidgetVisibilityOff(todoWidget)

        Log.d(ASSERTION_TAG, "Assert that both hidden widgets moved below the visible ones in the list.")
        customizeDashboardPage.assertWidgetOrder(welcomeWidget, coursesWidget, forecastWidget)
        customizeDashboardPage.assertWidgetOrder(welcomeWidget, coursesWidget, todoWidget)

        Log.d(ASSERTION_TAG, "Assert that the hidden widgets cannot be moved up or down anymore.")
        customizeDashboardPage.assertMoveButtonsDisabled(forecastWidget)
        customizeDashboardPage.assertMoveButtonsDisabled(todoWidget)

        Log.d(STEP_TAG, "Move the 'Welcome' widget down, below the 'Courses' widget.")
        customizeDashboardPage.moveWidgetDown(welcomeWidget)

        Log.d(ASSERTION_TAG, "Assert that the two visible widgets swapped places in the list.")
        customizeDashboardPage.assertWidgetOrder(coursesWidget, welcomeWidget)

        Log.d(ASSERTION_TAG, "Assert that the 'Courses' widget became the first one, so it cannot be moved up anymore.")
        customizeDashboardPage.assertMoveUpButtonDisabled(coursesWidget)

        Log.d(STEP_TAG, "Close the 'Customize Dashboard' page.")
        customizeDashboardPage.clickCloseButton()

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed again.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the widgets turned off on the 'Customize Dashboard' page are not displayed on the dashboard anymore.")
        newDashboardPage.assertWidgetNotDisplayed(forecastWidget)
        newDashboardPage.assertWidgetNotDisplayed(todoWidget)

        Log.d(ASSERTION_TAG, "Assert that the widgets left on are still displayed, in the new order.")
        newDashboardPage.assertWidgetDisplayed(coursesWidget)
        newDashboardPage.assertWidgetDisplayed(welcomeWidget)
        newDashboardPage.assertWidgetOrder(coursesWidget, welcomeWidget)

        Log.d(STEP_TAG, "Open the 'Customize Dashboard' page again, this time from the overflow menu in the top right corner.")
        newDashboardPage.openOverflowMenuCustomizeDashboard()

        Log.d(ASSERTION_TAG, "Assert that the 'Customize Dashboard' page is displayed.")
        customizeDashboardPage.assertPageDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the changes made previously were kept: the two visible widgets are in their swapped order and the other two are still turned off.")
        customizeDashboardPage.assertWidgetOrder(coursesWidget, welcomeWidget)
        customizeDashboardPage.assertWidgetVisibilityOn(coursesWidget)
        customizeDashboardPage.assertWidgetVisibilityOn(welcomeWidget)
        customizeDashboardPage.assertWidgetVisibilityOff(forecastWidget)
        customizeDashboardPage.assertWidgetVisibilityOff(todoWidget)

        Log.d(STEP_TAG, "Turn the visibility switch of the 'Weekly Summary' (forecast) widget back on.")
        customizeDashboardPage.toggleWidgetVisibility(forecastWidget)

        Log.d(ASSERTION_TAG, "Assert that the 'Weekly Summary' widget is turned on again and moved back above the still hidden 'To Do' widget, to the end of the visible ones.")
        customizeDashboardPage.assertWidgetVisibilityOn(forecastWidget)
        customizeDashboardPage.assertWidgetOrder(coursesWidget, welcomeWidget, forecastWidget, todoWidget)

        Log.d(STEP_TAG, "Turn the visibility switch of the 'To Do' widget back on.")
        customizeDashboardPage.toggleWidgetVisibility(todoWidget)

        Log.d(ASSERTION_TAG, "Assert that all four widgets are turned on again, with the 'To Do' widget at the end of the list.")
        customizeDashboardPage.assertWidgetVisibilityOn(todoWidget)
        customizeDashboardPage.assertWidgetOrder(coursesWidget, welcomeWidget, forecastWidget, todoWidget)

        Log.d(ASSERTION_TAG, "Assert that the widgets turned back on can be moved again.")
        customizeDashboardPage.assertMoveButtonsEnabled(forecastWidget)
        customizeDashboardPage.assertMoveUpButtonEnabled(todoWidget)

        Log.d(STEP_TAG, "Move the 'To Do' widget up, above the 'Weekly Summary' (forecast) widget.")
        customizeDashboardPage.moveWidgetUp(todoWidget)

        Log.d(ASSERTION_TAG, "Assert that the 'To Do' and 'Weekly Summary' widgets swapped places in the list.")
        customizeDashboardPage.assertWidgetOrder(coursesWidget, welcomeWidget, todoWidget, forecastWidget)

        Log.d(STEP_TAG, "Move the 'To Do' widget up once more, above the 'Welcome' widget.")
        customizeDashboardPage.moveWidgetUp(todoWidget)

        Log.d(ASSERTION_TAG, "Assert that the 'To Do' widget became the second one in the list.")
        customizeDashboardPage.assertWidgetOrder(coursesWidget, todoWidget, welcomeWidget, forecastWidget)

        Log.d(STEP_TAG, "Close the 'Customize Dashboard' page.")
        customizeDashboardPage.clickCloseButton()

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed again.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all four widgets are displayed on the dashboard again, in the order set on the 'Customize Dashboard' page.")
        newDashboardPage.assertWidgetDisplayed(coursesWidget)
        newDashboardPage.assertWidgetDisplayed(todoWidget)
        newDashboardPage.assertWidgetDisplayed(welcomeWidget)
        newDashboardPage.assertWidgetDisplayed(forecastWidget)
        newDashboardPage.assertWidgetOrder(coursesWidget, todoWidget, welcomeWidget, forecastWidget)

        Log.d(STEP_TAG, "Open the 'Customize Dashboard' page again to change the dashboard color.")
        newDashboardPage.openOverflowMenuCustomizeDashboard()

        Log.d(ASSERTION_TAG, "Assert that the 'Customize Dashboard' page is displayed.")
        customizeDashboardPage.assertPageDisplayed()

        Log.d(STEP_TAG, "Expand the dashboard color picker and select a new color.")
        customizeDashboardPage.expandDashboardColorPicker()
        val selectedColor = customizeDashboardPage.selectDashboardColor(R.color.courseColor1light)

        Log.d(ASSERTION_TAG, "Assert that the selected color is marked as selected in the color picker.")
        customizeDashboardPage.expandDashboardColorPicker()
        customizeDashboardPage.assertDashboardColorSelected(R.color.courseColor1light)

        Log.d(STEP_TAG, "Close the 'Customize Dashboard' page.")
        customizeDashboardPage.clickCloseButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Weekly Summary' widget is painted with the newly selected dashboard color.")
        newDashboardPage.assertDashboardDisplayed()
        newDashboardPage.assertForecastWidgetColor(selectedColor)

        Log.d(STEP_TAG, "Open the 'Customize Dashboard' page again to check the dashboard layout switch.")
        newDashboardPage.openOverflowMenuCustomizeDashboard()

        Log.d(ASSERTION_TAG, "Assert that the previously selected dashboard color was kept.")
        customizeDashboardPage.assertPageDisplayed()
        customizeDashboardPage.expandDashboardColorPicker()
        customizeDashboardPage.assertDashboardColorSelected(R.color.courseColor1light)

        Log.d(ASSERTION_TAG, "Assert that the 'New Mobile Dashboard' switch is turned on.")
        customizeDashboardPage.assertNewDashboardToggleOn()

        Log.d(STEP_TAG, "Turn off the 'New Mobile Dashboard' switch.")
        customizeDashboardPage.clickNewDashboardToggle()

        Log.d(ASSERTION_TAG, "Assert that the 'Changing dashboard layout' dialog is displayed with its message, its 'Restart now' and its 'Cancel' button.")
        customizeDashboardPage.assertChangingDashboardLayoutDialogDisplayed()

        Log.d(STEP_TAG, "Dismiss the dialog with its 'Cancel' button.")
        customizeDashboardPage.clickChangingDashboardLayoutDialogCancelButton()

        Log.d(ASSERTION_TAG, "Assert that the dialog is gone and the switch is still turned on, so cancelling keeps the new dashboard.")
        customizeDashboardPage.assertChangingDashboardLayoutDialogNotDisplayed()
        customizeDashboardPage.assertNewDashboardToggleOn()

        Log.d(STEP_TAG, "Turn off the 'New Mobile Dashboard' switch again.")
        customizeDashboardPage.clickNewDashboardToggle()

        Log.d(ASSERTION_TAG, "Assert that the 'Changing dashboard layout' dialog is displayed again.")
        customizeDashboardPage.assertChangingDashboardLayoutDialogDisplayed()

        Log.d(STEP_TAG, "Confirm the dialog with its 'Restart now' button.")
        customizeDashboardPage.clickChangingDashboardLayoutDialogRestartButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Switched back?' survey dialog is displayed with its message, all three options, its feedback, 'Skip' and 'Submit' buttons.")
        customizeDashboardPage.assertSurveyDialogDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the 'Submit' button is disabled until an option is selected.")
        customizeDashboardPage.assertSurveyDialogSubmitButtonDisabled()

        Log.d(ASSERTION_TAG, "Assert that confirming the dialog already turned the 'New Mobile Dashboard' switch off.")
        customizeDashboardPage.assertNewDashboardToggleOff()

        Log.d(STEP_TAG, "Select the 'Prefer the old layout' survey option.")
        customizeDashboardPage.selectSurveyOption(CustomizeDashboardPage.SurveyOption.PREFER_OLD_LAYOUT)

        Log.d(ASSERTION_TAG, "Assert that the 'Submit' button became enabled.")
        customizeDashboardPage.assertSurveyDialogSubmitButtonEnabled()

        // The test stops at the open survey dialog on purpose: 'Submit', 'Skip' and dismissing it all
        // restart the app with ProcessPhoenix, which kills the app process and with it the
        // instrumentation, so switching over to the old dashboard cannot be covered here. The turned
        // off preference does not leak into the other tests, as the widget config database is
        // per-user and every test seeds its own student.
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.NEW_DASHBOARD, TestCategory.E2E)
    fun testConferencesWidgetE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        val conferenceWebPage = ConferenceWebPage(device)
        val conferenceName = "E2E dashboard widget conference"

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginWithoutDashboardWait(student)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(STEP_TAG, "Open '${course.name}' course from the dashboard and navigate to its Conferences page.")
        newDashboardPage.clickCourse(course.id)
        courseBrowserPage.selectConferences()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed since no conference has been created yet.")
        conferenceListPage.assertEmptyView()

        Log.d(STEP_TAG, "Tap 'Open Externally' to open the conferences web page in a Chrome Custom Tab.")
        conferenceListPage.clickOnOpenExternallyButton()
        conferenceWebPage.waitForPageLoaded()

        Log.d(STEP_TAG, "Tap 'Add Conference' on the web page to open the create modal.")
        conferenceWebPage.clickAddConference()

        Log.d(ASSERTION_TAG, "Assert that the 'New Video Conference' modal is displayed with the expected fields.")
        conferenceWebPage.assertCreateModalDisplayed()

        Log.d(STEP_TAG, "Enter '$conferenceName' as the conference name and tap 'Create'.")
        conferenceWebPage.setConferenceName(conferenceName)
        conferenceWebPage.clickCreate()

        Log.d(ASSERTION_TAG, "Assert that '$conferenceName' appears under 'New Conferences' in the web list.")
        conferenceWebPage.assertConferenceInWebList(conferenceName)

        Log.d(STEP_TAG, "Tap 'Start' on the conference, then close the Custom Tab and return to the app.")
        conferenceWebPage.clickStart()
        conferenceWebPage.closeCustomTab()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that '$conferenceName' status is now 'In Progress' under 'New Conferences'.")
        conferenceListPage.assertConferenceStatus(conferenceName, "In Progress")
        conferenceListPage.assertNewConferencesDisplayed()

        Log.d(STEP_TAG, "Navigate back to the redesigned dashboard.")
        pressBackButton(2)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed again, where the Conferences widget should now show '$conferenceName'.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(STEP_TAG, "Refresh the dashboard.")
        newDashboardPage.refreshDashboard()

        Log.d(ASSERTION_TAG, "Assert that the Conferences widget header shows a count of 1 in-progress conference.")
        newDashboardPage.assertConferencesHeaderCount(1)

        Log.d(ASSERTION_TAG, "Assert that the Conferences widget shows a card for '${course.name}' with the 'Conference in progress' status.")
        newDashboardPage.assertConferenceDisplayed(course.id, course.name)

        Log.d(STEP_TAG, "Tap the Conferences widget card to join the conference.")
        newDashboardPage.clickConference(course.id)

        Log.d(ASSERTION_TAG, "Assert that tapping the widget opens the BigBlueButton join page in a Chrome Custom Tab.")
        conferenceWebPage.assertJoinPageDisplayed()

        Log.d(STEP_TAG, "Close the Custom Tab and return to the redesigned dashboard.")
        conferenceWebPage.closeCustomTab()
        newDashboardPage.assertDashboardDisplayed()

        Log.d(STEP_TAG, "Tap the 'X' button on the Conferences widget card to dismiss it.")
        newDashboardPage.dismissConference(course.id)

        Log.d(ASSERTION_TAG, "Assert that the Conferences widget card is no longer displayed after dismissing it.")
        newDashboardPage.assertConferenceGone(course.id)

        Log.d(ASSERTION_TAG, "Assert that a 'Conference dismissed' snackbar is displayed.")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        newDashboardPage.assertSnackbar(context.getString(R.string.conferencesWidgetDismissed))
    }
}
