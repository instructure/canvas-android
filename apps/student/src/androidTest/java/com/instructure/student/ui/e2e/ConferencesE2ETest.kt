package com.instructure.student.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.ConferencesApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ConferencesE2ETest: StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    // Fairly basic test that we can create and view a conference with the app.
    // I didn't attempt to actually start the conference because that goes through
    // an external web browser and would be really gross (if not impossible) to
    // test.
    //
    // Re-stubbing for now because the interface has changed from webview to native
    // and this test no longer passes.  MBL-14127 is being tracked to re-write this
    // test against the new native interface.
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CONFERENCES, TestCategory.E2E)
    fun testConferencesE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to ${course.name} course's Conferences Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectConferences()

        Log.d(STEP_TAG,"Assert that the empty view is displayed since we did not make any conference yet.")
        conferenceListPage.assertEmptyView()

        val testConferenceTitle = "E2E test conference"
        val testConferenceDescription = "Nightly E2E Test conference description"
        Log.d(PREPARATION_TAG,"Create a conference with '$testConferenceTitle' title and '$testConferenceDescription' description.")
        ConferencesApi.createCourseConference(teacher.token,
            testConferenceTitle, testConferenceDescription,"BigBlueButton",false,70,
            listOf(student.id),course.id)

        val testConferenceTitle2 = "E2E test conference 2"
        val testConferenceDescription2 = "Nightly E2E Test conference description 2"
        ConferencesApi.createCourseConference(teacher.token,
            testConferenceTitle2, testConferenceDescription2,"BigBlueButton",true,120,
            listOf(student.id),course.id)

        Log.d(STEP_TAG,"Refresh the page. Assert that $testConferenceTitle conference is displayed on the Conference List Page with the corresponding status.")
        refresh()
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle)
        conferenceListPage.assertConferenceStatus(testConferenceTitle,"Not Started")

        Log.d(STEP_TAG,"Assert that $testConferenceTitle2 conference is displayed on the Conference List Page with the corresponding status.")
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle2)
        conferenceListPage.assertConferenceStatus(testConferenceTitle2,"Not Started")

        Log.d(STEP_TAG,"Open '$testConferenceTitle' conference details page.")
        conferenceListPage.openConferenceDetails(testConferenceTitle)

        Log.d(STEP_TAG,"Assert that the proper conference title '$testConferenceTitle', status and description '$testConferenceDescription' are displayed.")
        conferenceDetailsPage.assertConferenceTitleDisplayed()
        conferenceDetailsPage.assertConferenceStatus("Not Started")
        conferenceDetailsPage.assertDescription(testConferenceDescription)

    }
}