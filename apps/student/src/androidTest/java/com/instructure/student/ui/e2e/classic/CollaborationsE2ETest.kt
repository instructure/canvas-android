package com.instructure.student.ui.e2e.classic

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.student.ui.pages.classic.CollaborationsPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


@HiltAndroidTest
class CollaborationsE2ETest: StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COLLABORATIONS, TestCategory.E2E)
    fun testCollaborationsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to '${course.name}' course's Collaborations Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectCollaborations()

        Log.d(ASSERTION_TAG, "Assert that various elements of the web page are present.")
        CollaborationsPage.assertCurrentCollaborationsHeaderPresent()

        Log.d(ASSERTION_TAG, "Assert that the 'Start a New Collaboration' button is displayed.")
        CollaborationsPage.assertStartANewCollaborationPresent()

        Log.d(ASSERTION_TAG, "Assert that within the selector, the 'Google Docs' has been selected as the default value.")
        CollaborationsPage.assertGoogleDocsChoicePresentAsDefaultOption()

        Log.d(ASSERTION_TAG, "Assert that the warning section (under the selector) of Google Docs has been displayed.")
        CollaborationsPage.assertGoogleDocsWarningDescriptionPresent()
    }

}