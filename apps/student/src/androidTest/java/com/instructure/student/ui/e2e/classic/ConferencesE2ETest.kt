package com.instructure.student.ui.e2e.classic

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.ConferencesApi
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ConferencesE2ETest: StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CONFERENCES, TestCategory.E2E)
    fun testConferencesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to '${course.name}' course's Conferences Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectConferences()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed since we did not make any conference yet.")
        conferenceListPage.assertEmptyView()

        val testConferenceTitle = "E2E test conference"
        val testConferenceDescription = "Nightly E2E Test conference description"
        Log.d(PREPARATION_TAG, "Create a conference with '$testConferenceTitle' title and '$testConferenceDescription' description.")
        ConferencesApi.createCourseConference(course.id, teacher.token, testConferenceTitle, testConferenceDescription, recipientUserIds = listOf(student.id))

        val testConferenceTitle2 = "E2E test conference 2"
        val testConferenceDescription2 = "Nightly E2E Test conference description 2"
        Log.d(PREPARATION_TAG, "Create a conference with '$testConferenceTitle2' title and '$testConferenceDescription2' description.")
        ConferencesApi.createCourseConference(course.id, teacher.token, testConferenceTitle2, testConferenceDescription2, longRunning = true, duration = 120, recipientUserIds = listOf(student.id))

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that '$testConferenceTitle' conference is displayed on the Conference List Page with the corresponding status and description.")
        refresh()
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle)
        conferenceListPage.assertConferenceStatus(testConferenceTitle,"Not Started")
        conferenceListPage.assertConferenceDescription(testConferenceTitle, testConferenceDescription)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Conferences' and there is the '${course.name}' course's name displayed as a subtitle.")
        conferenceListPage.assertConferencesToolbarText(course.name)

        Log.d(ASSERTION_TAG, "Assert that the 'New Conferences' text is displayed as a conference group type on the Conferences List Page.")
        conferenceListPage.assertNewConferencesDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the 'Open Externally' icon (button) is displayed on the top-right corner of the Conferences List Page.")
        conferenceListPage.assertOpenExternallyButtonDisplayed()

        Log.d(ASSERTION_TAG, "Assert that '$testConferenceTitle2' conference is displayed on the Conference List Page with the corresponding status and description.")
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle2)
        conferenceListPage.assertConferenceStatus(testConferenceTitle2,"Not Started")
        conferenceListPage.assertConferenceDescription(testConferenceTitle2, testConferenceDescription2)

        Log.d(STEP_TAG, "Open '$testConferenceTitle' conference's detailer page.")
        conferenceListPage.openConferenceDetails(testConferenceTitle)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Conference Details' and there is the '${course.name}' course's name displayed as a subtitle.")
        conferenceDetailsPage.assertConferenceDetailsToolbarText(course.name)

        Log.d(ASSERTION_TAG, "Assert that the proper conference title '$testConferenceTitle', status ('Not Started') and description, '$testConferenceDescription' are displayed.")
        conferenceDetailsPage.assertConferenceTitleDisplayed(testConferenceTitle)
        conferenceDetailsPage.assertConferenceStatus("Not Started")
        conferenceDetailsPage.assertDescription(testConferenceDescription)

    }

}