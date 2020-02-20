package com.instructure.student.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.CollaborationsPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

/**
 * Very basic test to verify that the collaborations web page shows up correctly.
 * We make no attempt to actually start a collaboration.
 * This test could break if changes are made to the web page that we bring up.
 */
class CollaborationsE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.COLLABORATIONS, TestCategory.E2E, false)
    fun testCollaborationsE2E() {

        // Seed basic student/teacher/course data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        // Sign the student in
        tokenLogin(student)
        dashboardPage.waitForRender()

        // Navigate to course collaborations
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectCollaborations()

        // Verify that various elements of the web page are present
        CollaborationsPage.assertCurrentCollaborationsHeaderPresent()
        CollaborationsPage.assertStartANewCollaborationPresent()
        CollaborationsPage.assertGoogleDocsChoicePresent()
        CollaborationsPage.assertGoogleDocsExplanationPresent()
    }
}