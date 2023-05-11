package com.emeritus.student.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.KnownBug
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.emeritus.student.ui.pages.CollaborationsPage
import com.emeritus.student.ui.utils.StudentTest
import com.emeritus.student.ui.utils.seedData
import com.emeritus.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


/**
 * Very basic test to verify that the collaborations web page shows up correctly.
 * We make no attempt to actually start a collaboration.
 * This test could break if changes are made to the web page that we bring up.
 */
@HiltAndroidTest
class CollaborationsE2ETest: StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @KnownBug("https://instructure.atlassian.net/browse/VICE-3157")
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COLLABORATIONS, TestCategory.E2E)
    fun testCollaborationsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to ${course.name} course's Collaborations Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectCollaborations()

        Log.d(STEP_TAG,"Verify that various elements of the web page are present.")
        CollaborationsPage.assertCurrentCollaborationsHeaderPresent()

        //On some screen size, this spinner does not displayed at all, instead of it,
        //there is a button on the top-right corner with the 'Start a new Collaboration' text
        //and clicking on it will 'expand' and display this spinner.
        //However, there is a bug (see link in this @KnownBug annotation) which is about the button not displayed on some screen size
        //So this test will breaks until it this ticket will be fixed.
        CollaborationsPage.assertStartANewCollaborationPresent()
        CollaborationsPage.assertGoogleDocsChoicePresent()
        CollaborationsPage.assertGoogleDocsExplanationPresent()
    }
}